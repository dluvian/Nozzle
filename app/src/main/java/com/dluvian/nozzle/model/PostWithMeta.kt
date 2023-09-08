package com.dluvian.nozzle.model

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.data.room.entity.PostEntity

data class PostWithMeta(
    val entity: PostEntity,
    val replyToName: String?,
    val replyToPubkey: String?,
    val pubkey: String,
    val annotatedContent: AnnotatedString,
    val mediaUrls: List<String>,
    val mentionedPosts: List<MentionedPost>,
    val name: String,
    val pictureUrl: String,
    val isLikedByMe: Boolean,
    val isFollowedByMe: Boolean,
    val isOneself: Boolean,
    val trustScore: Float?,
    val numOfReplies: Int,
    val relays: List<String>,
) {
    fun getPostIds(): PostIds {
        return PostIds(id = entity.id, replyToId = entity.replyToId)
    }
}
