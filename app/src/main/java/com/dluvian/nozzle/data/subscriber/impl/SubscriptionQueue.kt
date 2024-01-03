package com.dluvian.nozzle.data.subscriber.impl

import android.util.Log
import com.dluvian.nozzle.data.subscriber.ISubscriptionQueue
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay

class SubscriptionQueue : ISubscriptionQueue {
    override fun submitNoteId(noteId: NoteId, relays: Collection<Relay>?) {
        Log.i("LOLOL", "LOL")
    }

    override fun submitProfile(pubkey: Pubkey, relays: Collection<Relay>?) {
        Log.i("LOLOL", "LOL")
    }

    override fun submitNip65(pubkey: Pubkey, relays: Collection<Relay>?) {
        Log.i("LOLOL", "LOL")
    }

    override fun submitContactList(pubkey: Pubkey, relays: Collection<Relay>?) {
        Log.i("LOLOL", "LOL")
    }

    override fun submitNotes(
        until: Long,
        limit: Int,
        authors: Collection<Pubkey>?,
        hashtag: String?,
        mentionedPubkey: Pubkey?,
        relays: Collection<Relay>?
    ) {
        Log.i("LOLOL", "LOL")
    }

    override fun submitLikes(limit: Int, until: Long, author: Pubkey, relays: Collection<Relay>) {
        Log.i("LOLOL", "LOL")
    }

    override fun processNow() {
        Log.i("LOLOL", "LOL")
    }
}
