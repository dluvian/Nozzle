package com.dluvian.nozzle.data.subscriber.impl

import com.dluvian.nozzle.data.subscriber.ISubscriptionQueue
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay

class SubscriptionQueue : ISubscriptionQueue {
    override fun submitNoteId(noteId: NoteId, relays: Collection<Relay>?) {
        TODO("Not yet implemented")
    }

    override fun submitProfile(pubkey: Pubkey, relays: Collection<Relay>?) {
        TODO("Not yet implemented")
    }

    override fun submitNip65(pubkey: Pubkey, relays: Collection<Relay>?) {
        TODO("Not yet implemented")
    }

    override fun submitContactList(pubkey: Pubkey, relays: Collection<Relay>?) {
        TODO("Not yet implemented")
    }

    override fun submitNotes(
        until: Long,
        limit: Int,
        authors: Collection<Pubkey>?,
        hashtag: String?,
        mentionedPubkey: Pubkey?,
        relays: Collection<Relay>?
    ) {
        TODO("Not yet implemented")
    }

    override fun submitLikes(limit: Int, until: Long, author: Pubkey, relays: Collection<Relay>) {
        TODO("Not yet implemented")
    }

    override fun processNow() {
        TODO("Not yet implemented")
    }
}
