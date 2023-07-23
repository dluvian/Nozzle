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
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.utils.hexToNpub
import com.dluvian.nozzle.model.ProfileWithAdditionalInfo
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
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
    private val DEBOUNCE_MILLIS = 300L
    // TODO: Optimize debounce

    @OptIn(FlowPreview::class)
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
        ).distinctUntilChanged()

        return getFinalFlow(
            mainFlow = mainFlow,
            trustScoreFlow = trustScoreFlow,
            isFollowedByMeFlow = isFollowedByMeFlow,
            numOfFollowersFlow = numOfFollowersFlow
        )
    }

    private fun isOneself(pubkey: String) = pubkey == pubkeyProvider.getPubkey()

    @OptIn(FlowPreview::class)
    private fun getFinalFlow(
        mainFlow: Flow<ProfileWithAdditionalInfo>,
        trustScoreFlow: Flow<Float>,
        isFollowedByMeFlow: Flow<Boolean>,
        numOfFollowersFlow: Flow<Int>,
    ): Flow<ProfileWithAdditionalInfo> {
        var firstFinalEmit = true
        return combine(
            mainFlow.debounce {
                if (firstFinalEmit) {
                    firstFinalEmit = false
                    0
                } else 300
            },
            flow {
                emit(0f)
                emitAll(trustScoreFlow.debounce(DEBOUNCE_MILLIS))
            },
            flow {
                emit(false)
                emitAll(isFollowedByMeFlow.debounce(DEBOUNCE_MILLIS))
            },
            flow {
                emit(0)
                emitAll(numOfFollowersFlow.debounce(DEBOUNCE_MILLIS))
            },
        ) { main, trustScore, isFollowedByMe, numOfFollowers ->
            Log.d(TAG, "Combining profile final flow")
            main.copy(
                trustScore = trustScore,
                isFollowedByMe = isFollowedByMe,
                numOfFollowers = numOfFollowers
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun getMainFlow(
        baseFlow: Flow<ProfileWithAdditionalInfo>,
        relaysFlow: Flow<List<String>>,
        profileFlow: Flow<ProfileEntity?>,
        numOfFollowingFlow: Flow<Int>
    ): Flow<ProfileWithAdditionalInfo> {
        var firstMainEmit = true
        return combine(
            baseFlow,
            profileFlow.debounce {
                if (firstMainEmit) {
                    firstMainEmit = false
                    0
                } else 300
            },
            flow {
                emit(emptyList<String>())
                emitAll(relaysFlow.debounce(DEBOUNCE_MILLIS))
            },
            flow {
                emit(0)
                emitAll(numOfFollowingFlow.debounce(DEBOUNCE_MILLIS))
            },
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
        emit(
            ProfileWithAdditionalInfo(
                pubkey = pubkey,
                npub = npub,
                metadata = Metadata(),
                numOfFollowing = 0,
                numOfFollowers = 0,
                relays = emptyList(),
                isOneself = isOneself(pubkey = pubkey),
                isFollowedByMe = false,
                trustScore = if (isOneself(pubkey = pubkey)) null else 0f,
            )
        )
        nostrSubscriber.unsubscribeNip65()
        nostrSubscriber.unsubscribeProfiles()
        nostrSubscriber.subscribeNip65(listOf(pubkey))
        delay(1000)
        nostrSubscriber.subscribeToProfileMetadataAndContactList(
            pubkeys = listOf(pubkey),
            relays = nip65Dao.getWriteRelaysOfPubkey(pubkey = pubkey)
                .ifEmpty {
                    relaysFlow.firstOrNull().orEmpty().ifEmpty { getDefaultRelays() }
                }.shuffled()
                .take(5)  // Don't ask more than 5 relays
        )
    }
}
