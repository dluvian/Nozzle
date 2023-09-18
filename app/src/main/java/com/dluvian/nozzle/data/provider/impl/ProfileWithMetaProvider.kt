package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.profileIdToNostrId
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.helper.extended.ProfileEntityExtended
import com.dluvian.nozzle.data.utils.LONG_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.ProfileWithMeta
import com.dluvian.nozzle.model.helper.PubkeyVariations
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG = "ProfileWithMetaProvider"

class ProfileWithMetaProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrSubscriber: INostrSubscriber,
    private val relayProvider: IRelayProvider,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
    private val eventRelayDao: EventRelayDao,
) : IProfileWithMetaProvider {

    override fun getProfileFlow(profileId: String): Flow<ProfileWithMeta> {
        Log.i(TAG, "Get profile $profileId")

        val recommendedRelays = mutableListOf<String>()
        val pubkey = profileIdToNostrId(profileId)?.let {
            recommendedRelays.addAll(it.recommendedRelays)
            it.hex
        } ?: profileId

        makeSubscriptionAvailable()

        val profileExtendedFlow = profileDao.getProfileEntityExtendedFlow(
            pubkey = pubkey,
            personalPubkey = pubkeyProvider.getPubkey()
        ).distinctUntilChanged()

        // TODO: SQL join (?)
        val relaysFlow = eventRelayDao.listUsedRelaysFlow(pubkey)
            .firstThenDistinctDebounce(LONG_DEBOUNCE)

        // No debounce because of immediate user interaction response
        val trustScoreFlow = contactDao.getTrustScoreFlow(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkey = pubkey
        ).distinctUntilChanged()

        return getFinalFlow(
            pubkeyVariations = PubkeyVariations.fromPubkey(pubkey),
            profileFlow = profileExtendedFlow,
            relaysFlow = relaysFlow,
            trustScoreFlow = trustScoreFlow,
            recommendedRelays = recommendedRelays
        ).distinctUntilChanged()
    }

    private fun getFinalFlow(
        pubkeyVariations: PubkeyVariations,
        profileFlow: Flow<ProfileEntityExtended?>,
        relaysFlow: Flow<List<String>>,
        trustScoreFlow: Flow<Float>,
        recommendedRelays: List<String>,
    ): Flow<ProfileWithMeta> {
        return combine(
            profileFlow,
            relaysFlow,
            trustScoreFlow,
        ) { profile, relays, trustScore ->
            handleNostrSubscriptions(
                pubkey = pubkeyVariations.pubkey,
                recommendedRelays = recommendedRelays
            )
            ProfileWithMeta(
                pubkey = pubkeyVariations.pubkey,
                npub = pubkeyVariations.npub, // TODO: Cache npub
                metadata = profile?.profileEntity?.metadata
                    ?: Metadata(name = pubkeyVariations.shortenedNpub),
                numOfFollowing = profile?.numOfFollowing ?: 0,
                numOfFollowers = profile?.numOfFollowers ?: 0,
                relays = relays,
                isOneself = pubkeyProvider.isOneself(pubkeyVariations.pubkey),
                isFollowedByMe = profile?.isFollowedByMe ?: false,
                trustScore = trustScore,
            )
        }
    }

    private var subscribedToPubkey = ""

    private fun makeSubscriptionAvailable() {
        synchronized(subscribedToPubkey) {
            subscribedToPubkey = ""
        }
    }

    private suspend fun handleNostrSubscriptions(pubkey: String, recommendedRelays: List<String>) {
        synchronized(subscribedToPubkey) {
            if (subscribedToPubkey == pubkey) return
            subscribedToPubkey = pubkey
        }
        if (pubkey.isEmpty()) return

        nostrSubscriber.unsubscribeNip65()
        nostrSubscriber.unsubscribeProfileMetadataAndContactLists()
        nostrSubscriber.subscribeNip65(listOf(pubkey))
        nostrSubscriber.subscribeToProfileAndContactList(
            pubkeys = listOf(pubkey),
            relays = recommendedRelays + relayProvider.getWriteRelaysOfPubkey(pubkey = pubkey)
                // TODO: Fallback to post relays
                // TODO: Refactor into util function. Same in Profile view
                .let {
                    if (it.size > MAX_RELAYS) it.shuffled()
                        .sortedByDescending { relay ->
                            relayProvider.getReadRelays().contains(relay)
                        }
                        .take(MAX_RELAYS)
                    else it
                }
                .ifEmpty { relayProvider.getReadRelays() }
        )
    }
}
