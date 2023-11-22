package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class SimpleProfileProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
) : ISimpleProfileProvider {
    override fun getSimpleProfilesFlow(pubkeys: Collection<Pubkey>): Flow<List<SimpleProfile>> {
        if (pubkeys.isEmpty()) return flowOf(emptyList())

        val baseFlow = flowOf(pubkeys.toSet())
        val profileFlow = profileDao.listProfiles(pubkeys = pubkeys)
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
                    picture = profile?.metadata?.picture.orEmpty(),
                    pubkey = pubkey,
                    trustScore = score[pubkey] ?: 0f,
                    isOneself = pubkey == pubkeyProvider.getActivePubkey(),
                    isFollowedByMe = followers.contains(pubkey)
                )
            }
        }
    }

    override suspend fun getSimpleProfilesFlow(nameLike: String): Flow<List<SimpleProfile>> {
        if (nameLike.isBlank()) return flowOf(emptyList())

        val pubkeys = profileDao.getPubkeysWithNameLike(name = nameLike)
        if (pubkeys.isEmpty()) return flowOf(emptyList())

        return getSimpleProfilesFlow(pubkeys = pubkeys)
    }
}
