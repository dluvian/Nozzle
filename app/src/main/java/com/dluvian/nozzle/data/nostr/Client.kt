package com.dluvian.nozzle.data.nostr

import android.util.Log
import com.dluvian.nozzle.data.utils.JsonUtils
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

// TODO: Check concurrent modification (not just in this file)
class Client {
    private val httpClient = OkHttpClient()
    private val sockets: MutableMap<String, WebSocket> = Collections.synchronizedMap(mutableMapOf())
    private val subscriptions: MutableMap<String, WebSocket> =
        Collections.synchronizedMap(mutableMapOf())
    private var nostrListener: NostrListener? = null
    private val baseListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            nostrListener?.onOpen(response.message)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
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
                                    relayUrl = getRelayUrl(webSocket)
                                )
                            }
                    }

                    "OK" -> nostrListener?.onOk(id = msg[1].asString.orEmpty())
                    "NOTICE" -> nostrListener?.onError(msg = msg[1].asString)
                    "EOSE" -> nostrListener?.onEOSE(subscriptionId = msg[1].asString)
                    else -> nostrListener?.onError(msg = "Unknown type $type. Msg was $text")

                }
            } catch (t: Throwable) {
                nostrListener?.onError("Problem with $text", t)
                nostrListener?.onError("Queue size ${webSocket.queueSize()}", t)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            removeSocket(socket = webSocket)
            nostrListener?.onClose(reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            removeSocket(socket = webSocket)
            nostrListener?.onFailure(response?.message, t)
        }
    }

    fun subscribe(
        filters: List<Filter>,
        relays: Collection<String>? = null
    ): List<String> {
        if (filters.isEmpty()) {
            return listOf()
        }
        val ids = mutableListOf<String>()
        relays?.let { addRelays(relays) }
        val filteredSockets = sockets.entries
            .filter { relays?.contains(it.key) ?: true }
            .map { it.value }

        filteredSockets.forEach {
            val subscriptionId = UUID.randomUUID().toString()
            ids.add(subscriptionId)
            subscriptions[subscriptionId] = it
            val request = createSubscriptionRequest(subscriptionId, filters)
            Log.d(TAG, "Subscribe $request")
            it.send(request)
        }

        return ids
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

    fun publishToRelays(
        event: Event,
        relays: Collection<String>? = null
    ) {
        val request = """["EVENT",${event.toJson()}]"""
        Log.i(
            TAG,
            "Publish $request to ${relays?.size} relays"
        )
        relays?.let { addRelays(it) }
        for (relay in relays ?: sockets.keys) {
            val socket = sockets[relay]
            if (socket == null) Log.w(
                TAG,
                "Relay $relay is not registered"
            )
            else socket.send(request)
        }
    }

    fun addRelays(urls: Collection<String>) {
        urls.forEach { addRelay(it) }
    }

    private fun addRelay(url: String) {
        if (sockets.containsKey(url)) {
            return
        }
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
        sockets.keys.forEach { removeRelay(it) }
        httpClient.dispatcher.executorService.shutdown()
    }

    private fun getRelayUrl(webSocket: WebSocket): String? {
        return sockets.entries.find { it.value == webSocket }?.key
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
        Log.i(
            TAG,
            "Removed socket of $removedSubCount subscriptions"
        )
    }
}