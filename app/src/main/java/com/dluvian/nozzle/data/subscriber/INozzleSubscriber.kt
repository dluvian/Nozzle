package com.dluvian.nozzle.data.subscriber

import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.FeedInfo
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.feedFilter.RelayFilter

interface INozzleSubscriber {

    suspend fun subscribePersonalProfiles(relayFilter: RelayFilter)

    suspend fun subscribeUnknownContacts(relayFilter: RelayFilter)

    suspend fun subscribeUnknowns(notes: Collection<PostWithMeta>, relayFilter: RelayFilter)

    fun subscribeToFeed(
        pubkeysByRelay: Map<Relay, Set<Pubkey>?>,
        hashtag: String?,
        limit: Int,
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

    suspend fun subscribeFeedInfo(posts: List<PostEntity>, relayFilter: RelayFilter): FeedInfo

    // TODO: NostrId instead of String. Prevents parsing nostrStr multiple times
    suspend fun subscribeThreadPost(postId: String)

    suspend fun subscribeParentPost(noteId: NoteId, relayHint: Relay?)

    suspend fun subscribeNip65(pubkeys: Set<Pubkey>)

    fun subscribeToNotes(noteIds: Collection<NoteId>)
}
