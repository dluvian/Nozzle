package com.dluvian.nozzle.data.subscriber

import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay

interface ISubscriptionQueue {
    fun submitNoteId(noteId: NoteId, relays: Collection<Relay>?) {
        submitNoteIds(noteIds = listOf(noteId), relays = relays)
    }

    fun submitNoteIds(noteIds: Collection<NoteId>, relays: Collection<Relay>?)
    fun submitReplies(parentIds: Collection<NoteId>, relays: Collection<Relay>?)
    fun submitProfile(pubkey: Pubkey, relays: Collection<Relay>?) {
        submitProfiles(pubkeys = listOf(pubkey), relays = relays)
    }

    fun submitProfiles(pubkeys: Collection<Pubkey>, relays: Collection<Relay>?)
    fun submitNip65(pubkey: Pubkey, relays: Collection<Relay>?) {
        submitNip65s(pubkeys = listOf(pubkey), relays = relays)
    }

    fun submitNip65s(pubkeys: Collection<Pubkey>, relays: Collection<Relay>?)
    fun submitContactList(pubkey: Pubkey, relays: Collection<Relay>?) {
        submitContactLists(pubkeys = listOf(pubkey), relays = relays)
    }

    fun submitContactLists(pubkeys: Collection<Pubkey>, relays: Collection<Relay>?)
    fun submitNotes(
        until: Long,
        limit: Int,
        authors: Collection<Pubkey>?,
        hashtag: String?,
        mentionedPubkey: Pubkey?,
        relays: Collection<Relay>?
    )

    fun submitFeed(
        until: Long,
        limit: Int,
        authors: Collection<Pubkey>?,
        relays: Collection<Relay>?
    ) {
        submitNotes(
            until = until,
            limit = limit,
            authors = authors,
            hashtag = null,
            mentionedPubkey = null,
            relays = relays
        )
    }

    fun submitInbox(until: Long, limit: Int, mentionedPubkey: Pubkey, relays: Collection<Relay>) {
        submitNotes(
            until = until,
            limit = limit,
            authors = null,
            hashtag = null,
            mentionedPubkey = mentionedPubkey,
            relays = relays
        )
    }

    fun submitHashtag(until: Long, limit: Int, hashtag: String, relays: Collection<Relay>) {
        submitNotes(
            until = until,
            limit = limit,
            authors = null,
            hashtag = hashtag,
            mentionedPubkey = null,
            relays = relays
        )
    }

    fun submitLikes(limit: Int, until: Long, author: Pubkey, relays: Collection<Relay>)
    fun submitLikes(noteIds: Collection<NoteId>, relays: Collection<Relay>)

    fun submitFullProfile(pubkey: Pubkey, relays: Collection<Relay>?) {
        submitNip65(pubkey = pubkey, relays = null)
        submitProfile(pubkey = pubkey, relays = relays)
        submitContactList(pubkey = pubkey, relays = relays)
    }

    fun processNow()
}
