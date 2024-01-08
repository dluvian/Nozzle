package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNprofileStr
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.profileIdToNostrId
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.room.helper.extended.ProfileEntityExtended
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.utils.LONG_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.ProfileWithMeta
import com.dluvian.nozzle.model.PubkeyVariations
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG = "ProfileWithMetaProvider"

class ProfileWithMetaProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val nozzleSubscriber: INozzleSubscriber,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
    private val eventRelayDao: EventRelayDao,
    private val nip65Dao: Nip65Dao,
) : IProfileWithMetaProvider {

    override suspend fun getProfileFlow(profileId: String): Flow<ProfileWithMeta> {
        Log.i(TAG, "Get profile $profileId")

        nozzleSubscriber.subscribeFullProfile(profileId = profileId)

        val pubkey = profileIdToNostrId(profileId)?.hex ?: profileId

        val profileExtendedFlow = profileDao.getProfileEntityExtendedFlow(pubkey = pubkey)
            .distinctUntilChanged()

        // TODO: SQL join (?)
        val seenInRelaysFlow = eventRelayDao.listUsedRelaysFlow(pubkey)
            .firstThenDistinctDebounce(LONG_DEBOUNCE)

        val nip65Flow = nip65Dao.getNip65RelaysOfPubkeyFlow(pubkey = pubkey)
            .firstThenDistinctDebounce(LONG_DEBOUNCE)

        val nprofileFlow = seenInRelaysFlow.combine(nip65Flow) { seenIn, nip65s ->
            val writeRelays = nip65s.filter { it.isWrite }.map { it.url }.toSet()
            val relays = seenIn.intersect(writeRelays)
                .ifEmpty { seenIn }
                .ifEmpty { writeRelays }
                .take(MAX_RELAYS)
            createNprofileStr(pubkey = pubkey, relays = relays)
        }

        // No debounce because of immediate user interaction response
        val trustScoreFlow = contactDao
            .getTrustScoreFlow(contactPubkey = pubkey)
            .distinctUntilChanged()

        return getFinalFlow(
            pubkeyVariations = PubkeyVariations.fromPubkey(pubkey),
            profileFlow = profileExtendedFlow,
            seenInRelaysFlow = seenInRelaysFlow,
            nip65Flow = nip65Flow,
            nprofileFlow = nprofileFlow,
            trustScoreFlow = trustScoreFlow,
        ).distinctUntilChanged()
    }

    private fun getFinalFlow(
        pubkeyVariations: PubkeyVariations,
        profileFlow: Flow<ProfileEntityExtended?>,
        seenInRelaysFlow: Flow<List<String>>,
        nip65Flow: Flow<List<Nip65Relay>>,
        nprofileFlow: Flow<String?>,
        trustScoreFlow: Flow<Float>,
    ): Flow<ProfileWithMeta> {
        return combine(
            profileFlow,
            seenInRelaysFlow,
            nip65Flow,
            nprofileFlow,
            trustScoreFlow,
        ) { profile, seenInRelays, nip65s, nprofile, trustScore ->
            ProfileWithMeta(
                pubkey = pubkeyVariations.pubkey,
                nprofile = nprofile ?: pubkeyVariations.npub,
                metadata = profile?.profileEntity?.metadata
                    ?: Metadata(name = pubkeyVariations.shortenedNpub),
                numOfFollowing = profile?.numOfFollowing ?: 0,
                numOfFollowers = profile?.numOfFollowers ?: 0,
                seenInRelays = seenInRelays,
                writesInRelays = nip65s.filter { it.isWrite }.map { it.url },
                readsInRelays = nip65s.filter { it.isRead }.map { it.url },
                isOneself = pubkeyProvider.isOneself(pubkeyVariations.pubkey), // TODO: Handle in SQL
                followsYou = profile?.followsYou ?: false,
                trustScore = trustScore,
            )
        }
    }
}
