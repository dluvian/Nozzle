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
) : INostrService {
    private val client = Client(httpClient = httpClient)
    private val unsubOnEOSECache = Collections.synchronizedSet(mutableSetOf<String>())
    private val listener = object : NostrListener {
        override fun onOpen(relay: String, msg: String) {
            Log.i(TAG, "OnOpen($relay): $msg")
        }

        override fun onEvent(subscriptionId: String, event: Event, relayUrl: String?) {
            eventProcessor.submit(event = event, relayUrl = relayUrl)
        }

        override fun onError(relay: String, msg: String, throwable: Throwable?) {
            // TODO: Fix "bad req: uneven size input to from_hex"
            // TODO: Fix "invalid: "prefixOrAuthor" must only contain hexadecimal characters"
            Log.w(TAG, "OnError($relay): $msg", throwable)
        }

        override fun onEOSE(relay: String, subscriptionId: String) {
            Log.d(TAG, "OnEOSE($relay): $subscriptionId")
            if (unsubOnEOSECache.remove(subscriptionId)) {
                Log.d(TAG, "Unsubscribe onEOSE($relay) $subscriptionId")
                client.unsubscribe(subscriptionId)
            }
        }

        override fun onClosed(relay: String, subscriptionId: SubId, reason: String) {
            Log.d(TAG, "OnClosed($relay): $subscriptionId, reason: $reason")
            unsubOnEOSECache.remove(subscriptionId)
        }

        override fun onClose(relay: String, reason: String) {
            Log.i(TAG, "OnClose($relay): $reason")
        }

        override fun onFailure(relay: String, msg: String?, throwable: Throwable?) {
            Log.w(TAG, "OnFailure($relay): $msg", throwable)
        }

        override fun onOk(relay: String, id: String) {
            Log.d(TAG, "OnOk($relay): $id")
        }
    }

    override fun initialize(initRelays: Collection<String>) {
        client.setListener(listener)
        Log.i(TAG, "Add ${initRelays.size} relays")
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
        unsubOnEOSECache.add(subId)

        return subId
    }

    override fun unsubscribe(subscriptionIds: Collection<String>) {
        if (subscriptionIds.isEmpty()) return

        subscriptionIds.forEach {
            client.unsubscribe(it)
        }
    }

    override fun getActiveRelays(): List<Relay> {
        return client.getAllConnectedUrls()
    }

    override fun close() {
        Log.i(TAG, "Close connections")
        client.close()
    }
}
