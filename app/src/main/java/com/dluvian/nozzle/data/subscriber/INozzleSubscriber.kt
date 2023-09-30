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

    suspend fun subscribeFullProfile(profileId: String)

    suspend fun subscribeFeedInfo(posts: List<PostEntity>): FeedInfo

    suspend fun subscribeUnknowns(posts: List<PostWithMeta>)

    suspend fun subscribeParentPost(postId: String, relayHint: String?)
    suspend fun unsubscribeParentPosts()

    suspend fun subscribeNip65(pubkeys: List<String>)
}