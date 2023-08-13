package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.MAX_RELAY_REQUESTS
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.provider.IProfileWithAdditionalInfoProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.SHORT_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.data.utils.hexToNpub
import com.dluvian.nozzle.model.ProfileWithAdditionalInfo
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

private const val TAG = "ProfileWithAdditionalInfoProvider"

class ProfileWithAdditionalInfoProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrSubscriber: INostrSubscriber,
    private val relayProvider: IRelayProvider,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
    private val eventRelayDao: EventRelayDao,
    private val nip65Dao: Nip65Dao,
) : IProfileWithAdditionalInfoProvider {

    override fun getProfileFlow(pubkey: String): Flow<ProfileWithAdditionalInfo> {
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
        ).firstThenDistinctDebounce(SHORT_DEBOUNCE)

        return getFinalFlow(
            mainFlow = mainFlow,
            trustScoreFlow = trustScoreFlow,
            isFollowedByMeFlow = isFollowedByMeFlow,
            numOfFollowersFlow = numOfFollowersFlow
        ).distinctUntilChanged()
    }

    private fun getFinalFlow(
        mainFlow: Flow<ProfileWithAdditionalInfo>,
        trustScoreFlow: Flow<Float>,
        isFollowedByMeFlow: Flow<Boolean>,
        numOfFollowersFlow: Flow<Int>,
    ): Flow<ProfileWithAdditionalInfo> {
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
        baseFlow: Flow<ProfileWithAdditionalInfo>,
        relaysFlow: Flow<List<String>>,
        profileFlow: Flow<ProfileEntity?>,
        numOfFollowingFlow: Flow<Int>
    ): Flow<ProfileWithAdditionalInfo> {
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
            ProfileWithAdditionalInfo(
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
            relays = nip65Dao.getWriteRelaysOfPubkey(pubkey = pubkey)
                .ifEmpty { relaysFlow.firstOrNull().orEmpty() }
                // TODO: Take random n instead of shuffling whole list
                .let { if (it.size > MAX_RELAY_REQUESTS) it.shuffled() else it }
                .take(MAX_RELAY_REQUESTS)
                .ifEmpty { relayProvider.getReadRelays() }
        )
    }
}
