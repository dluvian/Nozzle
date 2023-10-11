package com.dluvian.nozzle.data.nostr

import com.dluvian.nozzle.data.room.helper.Nip65Relay
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

    fun sendLike(postId: String, postPubkey: String, relays: Collection<String>?): Event

    fun sendReply(
        replyTo: ReplyTo,
        content: String,
        mentions: List<String>,
        hashtags: List<String>,
        relays: Collection<String>?
    ): Event

    fun updateContactList(contactPubkeys: List<String>, relays: Collection<String>?): Event

    fun subscribe(
        filters: List<Filter>,
        unsubOnEOSE: Boolean,
        relays: Collection<String>?,
    ): List<String>

    fun unsubscribe(subscriptionIds: Collection<String>)

    fun close()
}
