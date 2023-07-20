package com.dluvian.nozzle.model

data class PostWithMeta(
    val id: String,
    val replyToId: String?,
    val replyToRootId: String?,
    val replyToName: String?,
    val replyToPubkey: String?,
    val replyRelayHint: String?,
    val pubkey: String,
    val createdAt: Long,
    val content: String,
    val name: String,
    val pictureUrl: String,
    val isLikedByMe: Boolean,
    val isRepostedByMe: Boolean,
    val isFollowedByMe: Boolean,
    val isOneself: Boolean,
    val followedByFriendsPercentage: Float?,
    val numOfReplies: Int,
    val repost: RepostPreview?,
    val relays: List<String>,
) {
    fun getPostIds(): PostIds {
        return PostIds(id = id, replyToId = replyToId, replyToRootId = replyToRootId)
    }
}
