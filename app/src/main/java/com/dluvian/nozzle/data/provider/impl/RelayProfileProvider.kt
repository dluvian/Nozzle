package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.provider.IOnlineStatusProvider
import com.dluvian.nozzle.data.provider.IRelayProfileProvider
import com.dluvian.nozzle.data.room.dao.RelayProfileDao
import com.dluvian.nozzle.data.room.entity.RelayProfileEntity
import com.dluvian.nozzle.data.utils.JsonUtils
import com.dluvian.nozzle.model.ItemWithOnlineStatus
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.Waiting
import com.dluvian.nozzle.model.relay.RelayProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

private const val TAG = "RelayProfileProvider"

class RelayProfileProvider(
    private val httpClient: OkHttpClient,
    private val relayProfileDao: RelayProfileDao,
    private val onlineStatusProvider: IOnlineStatusProvider
) : IRelayProfileProvider {
    val scope = CoroutineScope(Dispatchers.IO)
    override suspend fun getRelayProfile(relayUrl: Relay): Flow<ItemWithOnlineStatus<RelayProfile?>> {
        scope.launch { update(relayUrl = relayUrl) }
        return combine(
            relayProfileDao.getRelayProfileFlow(relayUrl = relayUrl),
            onlineStatusProvider.getOnlineStatuses(relays = listOf(relayUrl))
        ) { relayProfile, onlineStatus ->
            ItemWithOnlineStatus(
                item = relayProfile?.profile,
                onlineStatus = onlineStatus[relayUrl] ?: Waiting
            )
        }
    }

    override suspend fun update(relayUrl: Relay) {
        val request = Request.Builder()
            .url(relayUrl)
            .addHeader("Accept", "application/nostr+json")
            .build()
        kotlin.runCatching {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return
                JsonUtils.gson.fromJson(body, RelayProfile::class.java)
            }
        }.onSuccess { relayProfile ->
            val entity = RelayProfileEntity(relayUrl = relayUrl, profile = relayProfile)
            relayProfileDao.insertOrReplace(entity)
        }.onFailure {
            Log.w(TAG, "Failed to fetch relay profile of $relayUrl", it)
        }
    }
}
