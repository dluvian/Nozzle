package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.PING_INTERVAL
import com.dluvian.nozzle.data.PING_WAIT_TIME
import com.dluvian.nozzle.data.provider.IOnlineStatusProvider
import com.dluvian.nozzle.model.Offline
import com.dluvian.nozzle.model.Online
import com.dluvian.nozzle.model.OnlineStatus
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.Waiting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Collections

class OnlineStatusProvider(private val httpClient: OkHttpClient) : IOnlineStatusProvider {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val cache = Collections.synchronizedMap(mutableMapOf<Relay, OnlineStatus>())
    override fun getOnlineStatuses(relays: Collection<Relay>): Flow<Map<Relay, OnlineStatus>> {
        if (relays.isEmpty()) return flowOf(emptyMap())
        val distinctRelays = relays.toSet()
        return flow {
            ping(relays = distinctRelays)
            var lastPing = System.currentTimeMillis()

            while (true) {
                delay(PING_WAIT_TIME)
                emit(distinctRelays.associateWith { cache[it] ?: Waiting })
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastPing > PING_INTERVAL) {
                    lastPing = currentTime
                    ping(
                        relays = distinctRelays.shuffled()
                            .take(1.coerceAtLeast(distinctRelays.size / 2))
                    )
                }
            }
        }
    }

    private val jobs = mutableMapOf<Relay, Job>()

    private fun ping(relays: Collection<Relay>) {
        relays.forEach { relay ->
            synchronized(jobs) {
                if (jobs[relay]?.isActive != true) {
                    val request = Request.Builder()
                        .url(relay)
                        .build()
                    jobs[relay] = scope.launch {
                        cache[relay] = try {
                            httpClient.newCall(request).execute().use { response ->
                                val ping =
                                    response.receivedResponseAtMillis - response.sentRequestAtMillis
                                Online(ping = ping)
                            }
                        } catch (e: Exception) {
                            Offline
                        }
                    }
                }
            }
        }
    }
}
