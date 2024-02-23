package com.dluvian.nozzle.data.nostr

import android.util.Log
import com.dluvian.nozzle.data.eventProcessor.IEventProcessor
import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.model.EventId
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.SubId
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Filter
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.model.nostr.ReplyTo
import okhttp3.OkHttpClient
import java.util.Collections

private const val TAG = "NostrService"

class NostrService(
    httpClient: OkHttpClient,
    private val keyManager: IKeyManager,
    private val eventProcessor: IEventProcessor,
    private val filterCache: MutableMap<SubId, List<Filter>>
) : INostrService {
    private val client = Client(httpClient = httpClient)
    private val unsubOnEOSECache = Collections.synchronizedSet(mutableSetOf<String>())
    private val listener = object : NostrListener {
        override fun onOpen(relay: String, msg: String) {
            Log.i(TAG, "OnOpen($relay): $msg")
        }

        override fun onEvent(subscriptionId: SubId, event: Event, relayUrl: Relay?) {
            eventProcessor.submit(event = event, subId = subscriptionId, relayUrl = relayUrl)
        }

        override fun onError(relay: Relay, msg: String, throwable: Throwable?) {
            Log.w(TAG, "OnError($relay): $msg", throwable)
        }

        override fun onEOSE(relay: Relay, subscriptionId: SubId) {
            Log.d(TAG, "OnEOSE($relay): $subscriptionId")
            if (unsubOnEOSECache.remove(subscriptionId)) {
                Log.d(TAG, "Unsubscribe onEOSE($relay) $subscriptionId")
                client.unsubscribe(subscriptionId)
            }
            filterCache.remove(subscriptionId)
        }

        override fun onClosed(relay: Relay, subscriptionId: SubId, reason: String) {
            Log.d(TAG, "OnClosed($relay): $subscriptionId, reason: $reason")
            unsubOnEOSECache.remove(subscriptionId)
            filterCache.remove(subscriptionId)
        }

        override fun onClose(relay: Relay, reason: String) {
            Log.i(TAG, "OnClose($relay): $reason")
        }

        override fun onFailure(relay: Relay, msg: String?, throwable: Throwable?) {
            Log.w(TAG, "OnFailure($relay): $msg", throwable)
        }

        override fun onOk(relay: Relay, id: String, accepted: Boolean, msg: String) {
            Log.d(TAG, "OnOk($relay): $id, accepted=$accepted, ${msg.ifBlank { "No message" }}")
        }

        override fun onAuth(relay: Relay, challengeString: String) {
            Log.d(TAG, "OnAuth($relay): challenge=$challengeString")
            sendAuthentication(relay = relay, challengeString = challengeString)
        }
    }

    override fun initialize(initRelays: Collection<Relay>) {
        client.setListener(listener)
        Log.i(TAG, "Add ${initRelays.size} relays: $initRelays")
        client.addRelays(initRelays)
    }

    override fun publishProfile(metadata: Metadata, relays: Collection<String>?): Event {
        Log.i(TAG, "Publish profile $metadata")
        val event = Event.createMetadataEvent(
            metadata = metadata,
            keys = keyManager.getActiveKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun publishNip65(nip65Relays: List<Nip65Relay>): Event {
        val event = Event.createNip65Event(
            nip65Relays = nip65Relays,
            keys = keyManager.getActiveKeys(),
        )
        client.addRelays(nip65Relays.map { it.url })
        client.addRelays(getDefaultRelays())
        client.publishToRelays(event = event)

        return event
    }

    override fun sendPost(
        content: String,
        mentions: List<String>,
        hashtags: List<String>,
        relays: Collection<String>?
    ): Event {
        val event = Event.createTextNoteEvent(
            content = content,
            replyTo = null,
            mentions = mentions,
            hashtags = hashtags,
            keys = keyManager.getActiveKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun sendLike(
        postId: String,
        postPubkey: String,
        isRepost: Boolean,
        relays: Collection<String>?
    ): Event {
        Log.i(TAG, "Send like reaction for $postId to ${relays?.size} relays")
        val event = Event.createReactionEvent(
            eventId = postId,
            eventPubkey = postPubkey,
            isRepost = isRepost,
            keys = keyManager.getActiveKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun sendReply(
        replyTo: ReplyTo,
        content: String,
        mentions: List<String>,
        hashtags: List<String>,
        relays: Collection<String>?
    ): Event {
        val event = Event.createTextNoteEvent(
            content = content,
            replyTo = replyTo,
            mentions = mentions,
            hashtags = hashtags,
            keys = keyManager.getActiveKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun deleteEvent(eventId: EventId, seenInRelays: Collection<Relay>) {
        val event = Event.createDeleteEvent(
            eventId = eventId,
            keys = keyManager.getActiveKeys()
        )
        client.addRelays(seenInRelays)
        client.publishToRelays(event = event)
    }

    override fun updateContactList(
        contactPubkeys: List<String>,
        relays: Collection<String>?
    ): Event {
        Log.i(TAG, "Update contact list with ${contactPubkeys.size} contacts")
        val event = Event.createContactListEvent(
            contacts = contactPubkeys,
            keys = keyManager.getActiveKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun subscribe(filters: List<Filter>, relay: Relay): SubId? {
        val subId = client.subscribe(filters = filters, relay = relay)
        if (subId == null) {
            Log.w(TAG, "Failed to create subscription ID")
            return null
        }
        filterCache[subId] = filters
        unsubOnEOSECache.add(subId)

        return subId
    }

    override fun unsubscribe(subscriptionIds: Collection<SubId>) {
        if (subscriptionIds.isEmpty()) return

        subscriptionIds.forEach {
            client.unsubscribe(it)
        }
        subscriptionIds.forEach {
            filterCache.remove(it)
        }
    }

    override fun getActiveRelays(): List<Relay> {
        return client.getAllConnectedUrls()
    }

    override fun close() {
        Log.i(TAG, "Close connections")
        client.close()
    }

    private fun sendAuthentication(relay: Relay, challengeString: String) {
        val event = Event.createAuthEvent(
            relay = relay,
            challengeString = challengeString,
            keys = keyManager.getActiveKeys(),
        )
        client.publishAuth(authEvent = event, relay = relay)
    }
}
