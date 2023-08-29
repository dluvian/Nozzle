package com.dluvian.nozzle.data.nostr

import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Filter
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.model.nostr.ReplyTo

interface INostrService {
    fun initialize(initRelays: Collection<String>)

    fun publishProfile(metadata: Metadata): Event

    fun sendPost(content: String, relays: Collection<String>?): Event

    fun sendLike(postId: String, postPubkey: String, relays: Collection<String>?): Event

    fun sendReply(
        replyTo: ReplyTo,
        replyToPubkey: String,
        content: String,
        relays: Collection<String>?
    ): Event

    fun updateContactList(contactPubkeys: List<String>): Event

    fun subscribe(
        filters: List<Filter>,
        unsubOnEOSE: Boolean,
        relays: Collection<String>?,
    ): List<String>

    fun unsubscribe(subscriptionIds: List<String>)

    fun close()
}
