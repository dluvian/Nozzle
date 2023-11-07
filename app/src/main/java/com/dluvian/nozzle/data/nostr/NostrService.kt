package com.dluvian.nozzle.data.nostr

import android.util.Log
import com.dluvian.nozzle.data.eventProcessor.IEventProcessor
import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.room.helper.Nip65Relay
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
        override fun onOpen(msg: String) {
            Log.i(TAG, "OnOpen: $msg")
        }

        override fun onEvent(subscriptionId: String, event: Event, relayUrl: String?) {
            eventProcessor.submit(event = event, relayUrl = relayUrl)
        }

        override fun onError(msg: String, throwable: Throwable?) {
            Log.w(TAG, "OnError: $msg", throwable)
        }

        override fun onEOSE(subscriptionId: String) {
            Log.d(TAG, "OnEOSE: $subscriptionId")
            if (unsubOnEOSECache.remove(subscriptionId)) {
                Log.d(TAG, "Unsubscribe onEOSE $subscriptionId")
                client.unsubscribe(subscriptionId)
            }
        }

        override fun onClose(reason: String) {
            Log.i(TAG, "OnClose: $reason")
        }

        override fun onFailure(msg: String?, throwable: Throwable?) {
            Log.w(TAG, "OnFailure: $msg", throwable)
        }

        override fun onOk(id: String) {
            Log.d(TAG, "OnOk: $id")
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

    override fun sendLike(postId: String, postPubkey: String, relays: Collection<String>?): Event {
        Log.i(TAG, "Send like reaction for $postId to ${relays?.size} relays")
        val event = Event.createReactionEvent(
            eventId = postId,
            eventPubkey = postPubkey,
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

    override fun subscribe(
        filters: List<Filter>,
        unsubOnEOSE: Boolean,
        relays: Collection<String>?,
    ): List<String> {
        val subscriptionIds = client.subscribe(filters = filters, relays = relays)
        if (subscriptionIds.isEmpty()) return emptyList()
        if (unsubOnEOSE) unsubOnEOSECache.addAll(subscriptionIds)

        return subscriptionIds
    }

    override fun unsubscribe(subscriptionIds: Collection<String>) {
        if (subscriptionIds.isEmpty()) return

        subscriptionIds.forEach {
            client.unsubscribe(it)
        }
    }

    override fun close() {
        Log.i(TAG, "Close connections")
        client.close()
    }
}
