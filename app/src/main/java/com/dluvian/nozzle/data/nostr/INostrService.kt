package com.dluvian.nozzle.data.nostr

import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.model.EventId
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.SubId
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Filter
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.model.nostr.ReplyTo

interface INostrService {
    fun initialize(initRelays: Collection<String>)

    fun publishProfile(metadata: Metadata, relays: Collection<String>?): Event

    fun publishNip65(nip65Relays: List<Nip65Relay>): Event

    fun sendPost(
        content: String,
        mentions: List<String>,
        hashtags: List<String>,
        relays: Collection<String>?
    ): Event

    fun sendLike(
        postId: String,
        postPubkey: String,
        isRepost: Boolean,
        relays: Collection<String>?
    ): Event

    fun sendReply(
        replyTo: ReplyTo,
        content: String,
        mentions: List<String>,
        hashtags: List<String>,
        relays: Collection<String>?
    ): Event

    fun deleteEvent(eventId: EventId, seenInRelays: Collection<Relay> = emptyList())

    fun updateContactList(contactPubkeys: List<String>, relays: Collection<String>?): Event

    fun subscribe(filters: List<Filter>, relay: Relay): SubId?

    fun unsubscribe(subscriptionIds: Collection<String>)

    fun getActiveRelays(): List<Relay>

    fun close()
}
