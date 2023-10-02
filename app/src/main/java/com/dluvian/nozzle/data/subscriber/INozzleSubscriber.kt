package com.dluvian.nozzle.data.subscriber

import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RelaySelection
import com.dluvian.nozzle.model.helper.FeedInfo

interface INozzleSubscriber {

    fun subscribePersonalProfile()

    fun subscribeToFeedPosts(
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: List<String>?,
        limit: Int,
        until: Long?,
        relaySelection: RelaySelection,
    )

    // TODO: NostrId instead of String. Prevents parsing nostrStr multiple times
    suspend fun subscribeFullProfile(profileId: String)

    suspend fun subscribeFeedInfo(posts: List<PostEntity>): FeedInfo

    suspend fun subscribeUnknowns(posts: List<PostWithMeta>)

    // TODO: NostrId instead of String. Prevents parsing nostrStr multiple times
    suspend fun subscribeThreadPost(postId: String)

    suspend fun subscribeParentPost(postId: String, relayHint: String?)

    suspend fun subscribeNip65(pubkeys: List<String>)
}