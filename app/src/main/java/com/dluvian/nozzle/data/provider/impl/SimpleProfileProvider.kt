package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.MAX_LIST_LENGTH
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.WaitTime
import com.dluvian.nozzle.ui.app.views.profileList.ProfileListType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class SimpleProfileProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
) : ISimpleProfileProvider {
    override suspend fun getSimpleProfilesFlow(
        type: ProfileListType,
        pubkey: Pubkey,
        underPubkey: Pubkey,
        limit: Int,
        waitForSubscription: WaitTime
    ): Flow<List<SimpleProfile>> {
        if (limit <= 0) return flowOf(emptyList())

        val pubkeys = when (type) {
            ProfileListType.FOLLOWER_LIST -> contactDao.listContactPubkeys(pubkey = pubkey)
            ProfileListType.FOLLOWED_BY_LIST -> contactDao.listFollowedByPubkeys(pubkey = pubkey)
        }
        val pagedPubkeys = pubkeys.takeLastWhile { it != underPubkey }.take(limit)
        if (pagedPubkeys.isEmpty()) return flowOf(emptyList())

        return getFlow(pubkeys = pagedPubkeys)
    }

    override suspend fun getSimpleProfilesFlow(nameLike: String): Flow<List<SimpleProfile>> {
        if (nameLike.isBlank()) return flowOf(emptyList())

        val pubkeys = profileDao.getPubkeysWithNameLike(name = nameLike, limit = MAX_LIST_LENGTH)
        if (pubkeys.isEmpty()) return flowOf(emptyList())

        return getFlow(pubkeys = pubkeys)
    }

    override suspend fun getSimpleProfiles(
        nameLike: String,
        limit: Int
    ): List<SimpleProfile> {
        val pubkeys = profileDao
            .getPubkeysWithNameLike(name = nameLike.ifBlank { "a" }, limit = limit)
            .distinct()

        if (pubkeys.isEmpty()) return emptyList()

        val profiles = profileDao.getProfiles(pubkeys = pubkeys)
        val trustScores = contactDao.getTrustScoreByPubkey(contactPubkeys = pubkeys)
        val myFollowerList =
            contactDao.listContactPubkeys(pubkey = pubkeyProvider.getActivePubkey())

        return pubkeys.map { pubkey ->
            val profile = profiles.find { it.pubkey == pubkey }
            SimpleProfile(
                name = profile?.metadata?.name.orEmpty(),
                pubkey = pubkey,
                trustScore = trustScores[pubkey] ?: 0f,
                isOneself = pubkey == pubkeyProvider.getActivePubkey(),
                isFollowedByMe = myFollowerList.contains(pubkey)
            )
        }
    }

    private fun getFlow(pubkeys: Collection<Pubkey>): Flow<List<SimpleProfile>> {
        val baseFlow = flowOf(pubkeys.toSet())
        val profileFlow = profileDao.getProfilesFlow(pubkeys = pubkeys)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        val trustScoreFlow = contactDao.getTrustScoreByPubkeyFlow(contactPubkeys = pubkeys)
        val myFollowerListFlow = contactDao.listPersonalContactPubkeysFlow()

        return combine(
            baseFlow,
            profileFlow,
            trustScoreFlow,
            myFollowerListFlow
        ) { baseList, profiles, score, followers ->
            baseList.map { pubkey ->
                val profile = profiles.find { it.pubkey == pubkey }
                SimpleProfile(
                    name = profile?.metadata?.name.orEmpty(),
                    pubkey = pubkey,
                    trustScore = score[pubkey] ?: 0f,
                    isOneself = pubkey == pubkeyProvider.getActivePubkey(),
                    isFollowedByMe = followers.contains(pubkey)
                )
            }
        }
    }
}
