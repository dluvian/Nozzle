package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.data.annotatedContent.IAnnotatedContentHandler
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.MENTION_CHAR
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.hexToNpub
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.profileIdToNostrId
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.removeMentionCharOrNostrUri
import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.provider.IOnlineStatusProvider
import com.dluvian.nozzle.data.provider.IRelayProfileProvider
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.dao.RelayProfileDao
import com.dluvian.nozzle.data.room.entity.RelayProfileEntity
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.utils.JsonUtils
import com.dluvian.nozzle.model.ItemWithOnlineStatus
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.Waiting
import com.dluvian.nozzle.model.nostr.NpubNostrId
import com.dluvian.nozzle.model.nostr.nip11.Nip11Document
import com.dluvian.nozzle.model.relay.RelayProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Collections

private const val TAG = "RelayProfileProvider"

class RelayProfileProvider(
    private val httpClient: OkHttpClient,
    private val relayProfileDao: RelayProfileDao,
    private val onlineStatusProvider: IOnlineStatusProvider,
    private val annotatedContentHandler: IAnnotatedContentHandler,
    private val profileDao: ProfileDao,
    private val nozzleSubscriber: INozzleSubscriber
) : IRelayProfileProvider {
    val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun getRelayProfile(relayUrl: Relay): Flow<ItemWithOnlineStatus<RelayProfile?>> {
        scope.launch { update(relayUrl = relayUrl) }
        return combine(
            relayProfileDao.getRelayProfileFlow(relayUrl = relayUrl),
            onlineStatusProvider.getOnlineStatuses(relays = listOf(relayUrl))
        ) { relayProfile, onlineStatus ->
            ItemWithOnlineStatus(
                item = relayProfile?.let {
                    RelayProfile(
                        entity = it,
                        annotatedDescription = it.profile.description?.let { description ->
                            annotatedContentHandler.annotateContent(content = description)
                        },
                        annotatedPubkey = getAnnotatedPubkey(it.profile.pubkey),
                        annotatedPaymentsUrl = it.profile.paymentsUrl?.let { url ->
                            annotatedContentHandler.annotateContent(content = url)
                        },
                        annotatedSoftware = it.profile.software?.let { software ->
                            annotatedContentHandler.annotateContent(content = software)
                        },
                    )
                },
                onlineStatus = onlineStatus[relayUrl] ?: Waiting
            )
        }
    }

    override suspend fun update(relayUrl: Relay) {
        val request = Request.Builder()
            .url(relayUrl)
            .addHeader("Accept", "application/nostr+json")
            .build()
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return
                JsonUtils.gson.fromJson(body, Nip11Document::class.java)
            }
        }.onSuccess { nip11 ->
            val entity = RelayProfileEntity(
                relayUrl = relayUrl,
                profile = Nip11Document(
                    name = nip11.name?.trim(),
                    description = nip11.description?.trim(),
                    pubkey = nip11.pubkey?.trim(),
                    limitation = nip11.limitation,
                    paymentsUrl = nip11.paymentsUrl?.trim(),
                    software = nip11.software?.trim(),
                    version = nip11.version?.trim()
                )
            )
            relayProfileDao.insertOrReplace(entity)
        }.onFailure {
            Log.w(TAG, "Failed to fetch relay profile of $relayUrl", it)
        }
    }

    // TODO: Global name cache, use in ProfileProvider and elsewhere
    private val nameCache: MutableMap<Pubkey, String> =
        Collections.synchronizedMap(mutableMapOf())

    private suspend fun getAnnotatedPubkey(str: String?): AnnotatedString? {
        if (str == null) return null

        val cleanedStr = str.removeMentionCharOrNostrUri()
        val nostrId = if (KeyUtils.isValidHexKey(hexKey = cleanedStr)) {
            NpubNostrId(npub = hexToNpub(pubkey = cleanedStr), pubkeyHex = cleanedStr)
        } else {
            profileIdToNostrId(profileId = cleanedStr)
        }
        if (nostrId == null) return AnnotatedString(text = str)

        if (nameCache[nostrId.hex] == null) {
            nozzleSubscriber.subscribeSimpleProfiles(pubkeys = listOf(nostrId.hex))
            profileDao.getName(pubkey = nostrId.hex)?.let { dbName ->
                nameCache[nostrId.hex] = dbName
            }
        }

        return annotatedContentHandler.annotateContent(
            content = MENTION_CHAR + nostrId.nostrStr,
            mentionedNamesByPubkey = nameCache
        )
    }
}
