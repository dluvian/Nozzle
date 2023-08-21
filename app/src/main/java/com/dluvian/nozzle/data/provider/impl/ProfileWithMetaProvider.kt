package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.data.utils.hexToNpub
import com.dluvian.nozzle.model.ProfileWithMeta
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

private const val TAG = "ProfileWithMetaProvider"

class ProfileWithMetaProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrSubscriber: INostrSubscriber,
    private val relayProvider: IRelayProvider,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
    private val eventRelayDao: EventRelayDao,
) : IProfileWithMetaProvider {

    override fun getProfileFlow(pubkey: String): Flow<ProfileWithMeta> {
        Log.i(TAG, "Get profile $pubkey")
        val npub = hexToNpub(pubkey)
        val profileFlow = profileDao.getProfileFlow(pubkey)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        // TODO: SQL join (?)
        val relaysFlow = eventRelayDao.listUsedRelaysFlow(pubkey)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        // TODO: SQL join
        val numOfFollowingFlow = contactDao.countFollowingFlow(pubkey)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        // No debounce because of immediate user interaction response
        // TODO: SQL join
        val isFollowedByMeFlow = contactDao.isFollowedFlow(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkey = pubkey
        ).distinctUntilChanged()

        // No debounce because of immediate user interaction response
        // TODO: SQL join
        val numOfFollowersFlow = contactDao.countFollowersFlow(pubkey)
            .distinctUntilChanged()

        // No debounce because of immediate user interaction response
        val trustScoreFlow = contactDao.getTrustScoreFlow(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkey = pubkey
        ).distinctUntilChanged()

        val baseFlow = getBaseFlow(pubkey = pubkey, npub = npub, relaysFlow = relaysFlow)

        val mainFlow = getMainFlow(
            baseFlow = baseFlow,
            relaysFlow = relaysFlow,
            profileFlow = profileFlow,
            numOfFollowingFlow = numOfFollowingFlow
        )

        return getFinalFlow(
            mainFlow = mainFlow,
            trustScoreFlow = trustScoreFlow,
            isFollowedByMeFlow = isFollowedByMeFlow,
            numOfFollowersFlow = numOfFollowersFlow
        ).distinctUntilChanged()
    }

    private fun getFinalFlow(
        mainFlow: Flow<ProfileWithMeta>,
        trustScoreFlow: Flow<Float>,
        isFollowedByMeFlow: Flow<Boolean>,
        numOfFollowersFlow: Flow<Int>,
    ): Flow<ProfileWithMeta> {
        return combine(
            mainFlow,
            trustScoreFlow,
            isFollowedByMeFlow,
            numOfFollowersFlow,
        ) { main, trustScore, isFollowedByMe, numOfFollowers ->
            Log.d(TAG, "Combining profile final flow")
            main.copy(
                trustScore = trustScore,
                isFollowedByMe = isFollowedByMe,
                numOfFollowers = numOfFollowers
            )
        }
    }

    private fun getMainFlow(
        baseFlow: Flow<ProfileWithMeta>,
        relaysFlow: Flow<List<String>>,
        profileFlow: Flow<ProfileEntity?>,
        numOfFollowingFlow: Flow<Int>
    ): Flow<ProfileWithMeta> {
        return combine(
            baseFlow,
            profileFlow,
            relaysFlow,
            numOfFollowingFlow,
        ) { base, profile, relays, numOfFollowing ->
            Log.d(TAG, "Combining profile main flow")
            base.copy(
                metadata = profile?.getMetadata() ?: Metadata(),
                relays = relays,
                numOfFollowing = numOfFollowing
            )
        }
    }

    private fun getBaseFlow(pubkey: String, npub: String, relaysFlow: Flow<List<String>>) = flow {
        val isOneself = pubkeyProvider.isOneself(pubkey = pubkey)
        emit(
            ProfileWithMeta(
                pubkey = pubkey,
                npub = npub,
                metadata = Metadata(),
                numOfFollowing = 0,
                numOfFollowers = 0,
                relays = emptyList(),
                isOneself = isOneself,
                isFollowedByMe = false,
                trustScore = if (isOneself) null else 0.001f,
            )
        )
        nostrSubscriber.unsubscribeNip65()
        nostrSubscriber.unsubscribeProfiles()
        nostrSubscriber.subscribeNip65(listOf(pubkey))
        delay(WAIT_TIME)
        nostrSubscriber.subscribeToProfileMetadataAndContactList(
            pubkeys = listOf(pubkey),
            relays = relayProvider.getWriteRelaysOfPubkey(pubkey = pubkey)
                .ifEmpty { relaysFlow.firstOrNull().orEmpty() }
                // TODO: Refactor into util function. Same in Profile view
                .let {
                    if (it.size > MAX_RELAYS) it.shuffled()
                        .sortedByDescending { relay ->
                            relayProvider.getReadRelays().contains(relay)
                        }
                        .take(7)
                    else it
                }
                .ifEmpty { relayProvider.getReadRelays() }
        )
    }
}
