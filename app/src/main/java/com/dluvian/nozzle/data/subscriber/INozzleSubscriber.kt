package com.dluvian.nozzle.data.subscriber

import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.FeedInfo
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.RelaySelection

interface INozzleSubscriber {

    suspend fun subscribePersonalProfiles()

    suspend fun subscribeUnknownContacts()

    suspend fun subscribeUnknowns(notes: Collection<PostWithMeta>)

    fun subscribeToFeed(
        limit: Int,
        authors: List<Pubkey>?,
        relaySelection: RelaySelection,
        until: Long = getCurrentTimeInSeconds(),
    )

    fun subscribeToHashtag(
        limit: Int,
        hashtag: String,
        relays: Collection<Relay>,
        until: Long = getCurrentTimeInSeconds(),
    )

    fun subscribeToInbox(
        limit: Int,
        relays: Collection<String>,
        until: Long = getCurrentTimeInSeconds()
    )

    fun subscribeToLikes(
        limit: Int,
        until: Long = getCurrentTimeInSeconds()
    )

    // TODO: NostrId instead of String. Prevents parsing nostrStr multiple times
    suspend fun subscribeFullProfile(profileId: String)

    suspend fun subscribeSimpleProfiles(pubkeys: Collection<Pubkey>)

    suspend fun subscribeFeedInfo(posts: List<PostEntity>): FeedInfo

    // TODO: NostrId instead of String. Prevents parsing nostrStr multiple times
    suspend fun subscribeThreadPost(postId: String)

    suspend fun subscribeParentPost(noteId: NoteId, relayHint: Relay?)

    suspend fun subscribeNip65(pubkeys: Set<Pubkey>)

    fun subscribeToNotes(noteIds: Collection<NoteId>)
}
