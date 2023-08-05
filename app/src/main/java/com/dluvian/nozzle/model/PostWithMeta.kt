package com.dluvian.nozzle.model

data class PostWithMeta(
    val id: String,
    val replyToId: String?,
    val replyToName: String?,
    val replyToPubkey: String?,
    val replyRelayHint: String?,
    val pubkey: String,
    val createdAt: Long,
    val content: String,
    val name: String, // TODO: Nullable
    val pictureUrl: String, // TODO: Nullable
    val isLikedByMe: Boolean,
    val isFollowedByMe: Boolean,
    val isOneself: Boolean,
    val trustScore: Float?,
    val numOfReplies: Int,
    val relays: List<String>,
    val mentionedPost: MentionedPost?,
) {
    fun getPostIds(): PostIds {
        return PostIds(id = id, replyToId = replyToId)
    }
}
