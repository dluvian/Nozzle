package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNprofileStr
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.profileIdToNostrId
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.helper.extended.ProfileEntityExtended
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.utils.LONG_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.ProfileWithMeta
import com.dluvian.nozzle.model.helper.PubkeyVariations
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val TAG = "ProfileWithMetaProvider"

class ProfileWithMetaProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val nozzleSubscriber: INozzleSubscriber,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
    private val eventRelayDao: EventRelayDao,
) : IProfileWithMetaProvider {

    override suspend fun getProfileFlow(profileId: String): Flow<ProfileWithMeta> {
        Log.i(TAG, "Get profile $profileId")

        nozzleSubscriber.subscribeFullProfile(profileId = profileId)

        val pubkey = profileIdToNostrId(profileId)?.hex ?: profileId


        val profileExtendedFlow = profileDao.getProfileEntityExtendedFlow(pubkey = pubkey)
            .distinctUntilChanged()

        // TODO: SQL join (?)
        val relaysFlow = eventRelayDao.listUsedRelaysFlow(pubkey)
            .firstThenDistinctDebounce(LONG_DEBOUNCE)

        // TODO: Don't use every relay
        val nprofileFlow = relaysFlow.map { createNprofileStr(pubkey = pubkey, relays = it) }

        // No debounce because of immediate user interaction response
        val trustScoreFlow = contactDao.getTrustScoreFlow(
            pubkey = pubkeyProvider.getActivePubkey(),
            contactPubkey = pubkey
        ).distinctUntilChanged()

        return getFinalFlow(
            pubkeyVariations = PubkeyVariations.fromPubkey(pubkey),
            profileFlow = profileExtendedFlow,
            relaysFlow = relaysFlow,
            nprofileFlow = nprofileFlow,
            trustScoreFlow = trustScoreFlow,
        ).distinctUntilChanged()
    }

    private fun getFinalFlow(
        pubkeyVariations: PubkeyVariations,
        profileFlow: Flow<ProfileEntityExtended?>,
        relaysFlow: Flow<List<String>>,
        nprofileFlow: Flow<String?>,
        trustScoreFlow: Flow<Float>,
    ): Flow<ProfileWithMeta> {
        return combine(
            profileFlow,
            relaysFlow,
            nprofileFlow,
            trustScoreFlow,
        ) { profile, relays, nprofile, trustScore ->
            ProfileWithMeta(
                pubkey = pubkeyVariations.pubkey,
                nprofile = nprofile ?: pubkeyVariations.npub,
                metadata = profile?.profileEntity?.metadata
                    ?: Metadata(name = pubkeyVariations.shortenedNpub),
                numOfFollowing = profile?.numOfFollowing ?: 0,
                numOfFollowers = profile?.numOfFollowers ?: 0,
                relays = relays,
                isOneself = pubkeyProvider.isOneself(pubkeyVariations.pubkey),
                trustScore = trustScore,
            )
        }
    }
}
