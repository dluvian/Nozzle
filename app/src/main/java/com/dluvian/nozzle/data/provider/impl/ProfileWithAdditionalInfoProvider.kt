package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.provider.IProfileWithAdditionalInfoProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.utils.hexToNpub
import com.dluvian.nozzle.model.ProfileWithAdditionalInfo
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

private const val TAG = "ProfileWithAdditionalInfoProvider"

class ProfileWithAdditionalInfoProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrSubscriber: INostrSubscriber,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
    private val eventRelayDao: EventRelayDao,
    private val nip65Dao: Nip65Dao,
) : IProfileWithAdditionalInfoProvider {

    override fun getProfileFlow(pubkey: String): Flow<ProfileWithAdditionalInfo> {
        Log.i(TAG, "Get profile $pubkey")
        val npub = hexToNpub(pubkey)
        val profileFlow = profileDao.getProfileFlow(pubkey).distinctUntilChanged()
        val relaysFlow = eventRelayDao.listUsedRelaysFlow(pubkey).distinctUntilChanged()
        val numOfFollowingFlow = contactDao.countFollowingFlow(pubkey).distinctUntilChanged()
        val numOfFollowersFlow = contactDao.countFollowersFlow(pubkey).distinctUntilChanged()
        val isFollowedByMeFlow = contactDao.isFollowedFlow(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkey = pubkey
        ).distinctUntilChanged()
        val followedByFriendsPercentageFlow = contactDao.getFollowedByFriendsPercentageFlow(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkey = pubkey
        ).distinctUntilChanged()

        val mainFlow = flow {
            emit(
                ProfileWithAdditionalInfo(
                    pubkey = pubkey,
                    npub = npub,
                    metadata = Metadata(name = npub),
                    numOfFollowing = 0,
                    numOfFollowers = 0,
                    relays = listOf(),
                    isOneself = isOneself(pubkey = pubkey),
                    isFollowedByMe = false,
                    followedByFriendsPercentage = if (isOneself(pubkey = pubkey)) null else 0f,
                )
            )
            nostrSubscriber.unsubscribeNip65()
            nostrSubscriber.unsubscribeProfiles()
            nostrSubscriber.subscribeNip65(listOf(pubkey))
            delay(1000)
            nostrSubscriber.subscribeToProfileMetadataAndContactList(
                pubkeys = listContactPubkeysIfIsOneself(pubkey = pubkey),
                relays = nip65Dao.getWriteRelaysOfPubkey(pubkey = pubkey)
                    .ifEmpty {
                        relaysFlow.first().ifEmpty { getDefaultRelays() }
                    }.shuffled()
                    .take(10)  // Don't ask more than 10 relays
            )
        }
        return mainFlow
            .combine(profileFlow) { main, profile ->
                profile?.let { main.copy(metadata = profile.getMetadata()) } ?: main
            }
            .combine(relaysFlow) { main, relays ->
                main.copy(relays = relays)
            }
            .combine(numOfFollowingFlow) { main, numOfFollowing ->
                main.copy(numOfFollowing = numOfFollowing)
            }
            .combine(numOfFollowersFlow) { main, numOfFollowers ->
                main.copy(numOfFollowers = numOfFollowers)
            }
            .combine(isFollowedByMeFlow) { main, isFollowedByMe ->
                main.copy(isFollowedByMe = isFollowedByMe)
            }
            .combine(followedByFriendsPercentageFlow) { main, followedByFriendsPercentage ->
                main.copy(followedByFriendsPercentage = followedByFriendsPercentage)
            }
    }


    private fun isOneself(pubkey: String) = pubkey == pubkeyProvider.getPubkey()

    private suspend fun listContactPubkeysIfIsOneself(pubkey: String): List<String> {
        return if (isOneself(pubkey = pubkey)) {
            contactDao.listContactPubkeys(pubkey) + pubkey
        } else listOf(pubkey)
    }
}
