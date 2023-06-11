package com.dluvian.nozzle.data.nostr

import android.util.Log
import com.dluvian.nozzle.data.eventProcessor.IEventProcessor
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.model.nostr.ContactListEntry
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Filter
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.model.nostr.Post
import com.dluvian.nozzle.model.nostr.ReplyTo
import com.dluvian.nozzle.model.nostr.RepostId
import java.util.Collections

private const val TAG = "NostrService"

class NostrService(
    private val keyManager: IKeyManager,
    relayProvider: IRelayProvider,
    private val eventProcessor: IEventProcessor
) : INostrService {
    private val client = Client()
    private val unsubOnEOSECache = Collections.synchronizedSet(mutableSetOf<String>())
    private val listener = object : NostrListener {
        override fun onOpen(msg: String) {
            Log.i(TAG, "OnOpen: $msg")
        }

        override fun onEvent(subscriptionId: String, event: Event, relayUrl: String?) {
            Log.d(
                TAG,
                "OnEvent: id ${event.id}, kind ${event.kind}, relay $relayUrl in subscription $subscriptionId"
            )
            eventProcessor.process(event = event, relayUrl = relayUrl)
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

    init {
        client.setListener(listener)
        val relays = (relayProvider.getReadRelays() + relayProvider.getWriteRelays()).distinct()
        Log.i(TAG, "Add ${relays.size} relays")
        client.addRelays(relays)
    }

    override fun publishProfile(metadata: Metadata): Event {
        Log.i(TAG, "Publish profile $metadata")
        val event = Event.createMetadataEvent(
            metadata = metadata,
            keys = keyManager.getKeys(),
        )
        Log.i(TAG, "new profile is valid ${event.verify()}")
        client.publishToRelays(event)

        return event
    }

    override fun sendPost(content: String, relays: Collection<String>?): Event {
        Log.i(TAG, "Send post '${content.take(50)}'")
        val event = Event.createTextNoteEvent(
            post = Post(msg = content),
            keys = keyManager.getKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun sendRepost(
        postId: String,
        postPubkey: String,
        quote: String,
        originUrl: String,
        relays: Collection<String>?
    ): Event {
        Log.i(TAG, "Send repost of $postId")
        val event = Event.createTextNoteEvent(
            post = Post(
                repostId = RepostId(
                    repostId = postId,
                    relayUrl = originUrl
                ),
                mentions = listOf(postPubkey),
                msg = quote,
            ),
            keys = keyManager.getKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun sendLike(postId: String, postPubkey: String, relays: Collection<String>?): Event {
        Log.i(TAG, "Send like reaction to $postId")
        val event = Event.createReactionEvent(
            eventId = postId,
            eventPubkey = postPubkey,
            isPositive = true,
            keys = keyManager.getKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun sendReply(
        replyTo: ReplyTo,
        replyToPubkey: String,
        content: String,
        relays: Collection<String>?
    ): Event {
        Log.i(TAG, "Send reply to ${replyTo.replyTo} from author $replyToPubkey")
        val event = Event.createTextNoteEvent(
            post = Post(replyTo = replyTo, msg = content, mentions = listOf(replyToPubkey)),
            keys = keyManager.getKeys(),
        )
        client.publishToRelays(event = event, relays = relays)

        return event
    }

    override fun updateContactList(contacts: List<ContactListEntry>): Event {
        Log.i(TAG, "Update contact list with ${contacts.size} contacts")
        val event = Event.createContactListEvent(
            contacts = contacts,
            keys = keyManager.getKeys(),
        )
        client.publishToRelays(event = event)

        return event
    }

    override fun subscribe(
        filters: List<Filter>,
        unsubOnEOSE: Boolean,
        relays: Collection<String>?,
    ): List<String> {
        val subscriptionIds = client.subscribe(filters = filters, relays = relays)
        if (subscriptionIds.isNotEmpty() && unsubOnEOSE) unsubOnEOSECache.addAll(subscriptionIds)

        return subscriptionIds
    }

    override fun unsubscribe(subscriptionIds: List<String>) {
        if (subscriptionIds.isNotEmpty()) {
            subscriptionIds.forEach {
                client.unsubscribe(it)
            }
        }
    }

    override fun close() {
        Log.i(TAG, "Close connections")
        client.close()
    }
}
