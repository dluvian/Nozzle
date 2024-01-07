package com.dluvian.nozzle.data.nostr

import android.util.Log
import com.dluvian.nozzle.data.utils.JsonUtils
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.SubId
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Filter
import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.Collections
import java.util.UUID

private const val TAG = "Client"

// TODO: Disconnect bad relays and remember them

class Client(private val httpClient: OkHttpClient) {
    private val sockets: MutableMap<Relay, WebSocket> = Collections.synchronizedMap(mutableMapOf())
    private val subscriptions: MutableMap<SubId, WebSocket> =
        Collections.synchronizedMap(mutableMapOf())
    private var nostrListener: NostrListener? = null
    private val baseListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            nostrListener?.onOpen(relay = getRelayUrl(webSocket).orEmpty(), msg = response.message)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val relay = getRelayUrl(webSocket).orEmpty()
            try {
                val msg = JsonUtils.gson.fromJson(text, JsonElement::class.java).asJsonArray
                val type = msg[0].asString
                when (type) {
                    "EVENT" -> {
                        Event.fromJson(msg[2])
                            .onSuccess { event ->
                                nostrListener?.onEvent(
                                    subscriptionId = msg[1].asString,
                                    event = event,
                                    relayUrl = relay
                                )
                            }
                    }

                    "OK" -> nostrListener?.onOk(
                        relay = relay,
                        id = msg[1].asString.orEmpty(),
                        accepted = msg[2].asBoolean,
                        msg = msg[3].asString
                    )

                    "NOTICE" -> nostrListener?.onError(
                        relay = relay,
                        msg = "onNotice: ${msg[1].asString}"
                    )

                    "EOSE" -> nostrListener?.onEOSE(
                        relay = relay,
                        subscriptionId = msg[1].asString
                    )

                    "CLOSED" -> nostrListener?.onClosed(
                        relay = relay,
                        subscriptionId = msg[1].asString,
                        reason = msg[2].asString
                    )

                    else -> nostrListener?.onError(
                        relay = relay,
                        msg = "Unknown type $type. Msg was $text"
                    )

                }
            } catch (t: Throwable) {
                nostrListener?.onError(
                    relay = relay,
                    msg = "Problem with $text, Queue size ${webSocket.queueSize()}",
                    throwable = t
                )
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            val url = getRelayUrl(webSocket).orEmpty()
            removeSocket(socket = webSocket)
            nostrListener?.onClose(relay = url, reason = reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            val url = getRelayUrl(webSocket).orEmpty()
            removeSocket(socket = webSocket)
            nostrListener?.onFailure(relay = url, msg = response?.message, throwable = t)
        }
    }

    fun subscribe(filters: List<Filter>, relay: Relay): SubId? {
        if (filters.isEmpty()) return null

        addRelays(urls = listOf(relay))
        val socket = sockets[relay]
        if (socket == null) {
            Log.w(TAG, "Failed to sub ${filters.size} filters. Relay $relay is not registered")
            return null
        }
        val subId = UUID.randomUUID().toString()
        subscriptions[subId] = socket
        val request = createSubscriptionRequest(subId, filters)
        Log.d(TAG, "Subscribe in $relay: $request")
        socket.send(request)

        return subId
    }

    private fun createSubscriptionRequest(
        subscriptionId: String,
        filters: List<Filter>
    ): String {
        return """["REQ","$subscriptionId",${filters.joinToString(",") { it.toJson() }}]"""
    }

    fun unsubscribe(subscriptionId: String) {
        subscriptions[subscriptionId]?.let { socket ->
            Log.d(TAG, "Unsubscribe from $subscriptionId")
            socket.send("""["CLOSE","$subscriptionId"]""")
            subscriptions.remove(subscriptionId)
        }
    }

    fun publishToRelays(event: Event, relays: Collection<String>? = null) {
        relays?.let { addRelays(it) }
        val filteredRelays = filterSocketsByRelays(relays = relays)

        val request = """["EVENT",${event.toJson()}]"""
        Log.i(TAG, "Publish to ${filteredRelays.size} relays: $request")
        filteredRelays.forEach { it.value.send(request) }
    }

    fun addRelays(urls: Collection<String>) {
        urls.forEach { addRelay(it) }
    }

    fun getAllConnectedUrls(): List<Relay> {
        return sockets.keys.toList()
    }

    private fun addRelay(url: String) {
        if (sockets.containsKey(url)) return
        if (!url.startsWith("wss://")) return

        Log.i(TAG, "Add relay $url")
        try {
            val request = Request.Builder().url(url).build()
            val socket = httpClient.newWebSocket(request = request, listener = baseListener)
            sockets[url] = socket
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to connect to $url", t)
        }
    }

    private fun removeRelay(url: String) {
        Log.i(TAG, "Remove relay $url")
        sockets[url]?.close(1000, "Normal closure")
        sockets.remove(url)
    }

    fun setListener(listener: NostrListener) {
        Log.i(TAG, "Set listener")
        nostrListener = listener
    }

    fun close() {
        Log.i(TAG, "Close connections")
        synchronized(sockets) {
            sockets.keys.forEach { removeRelay(it) }
        }
        httpClient.dispatcher.executorService.shutdown()
    }

    private fun getRelayUrl(webSocket: WebSocket): String? {
        synchronized(sockets) {
            return sockets.entries.find { it.value == webSocket }?.key
        }
    }

    private fun removeSocket(socket: WebSocket) {
        val removedUrls = mutableSetOf<String>()
        var removedSubCount = 0
        synchronized(sockets) {
            sockets.filter { it.value == socket }.forEach {
                removedUrls.add(it.key)
                sockets.remove(it.key)
            }
        }
        synchronized(subscriptions) {
            subscriptions.filter { it.value == socket }.forEach {
                removedSubCount += 1
                subscriptions.remove(it.key)
            }
        }
        Log.i(TAG, "Removed socket of $removedUrls")
        Log.i(TAG, "Removed socket of $removedSubCount subscriptions")
    }

    private fun filterSocketsByRelays(relays: Collection<String>?): List<Map.Entry<String, WebSocket>> {
        synchronized(sockets) {
            return sockets.entries.filter { relays?.contains(it.key) ?: true }
        }
    }
}
