package com.dluvian.nozzle.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.data.room.entity.PostEntity

@Immutable
data class PostWithMeta(
    val entity: PostEntity,
    val replyToName: String?,
    val replyToPubkey: String?,
    val pubkey: String,
    val annotatedContent: AnnotatedString,
    val mediaUrls: List<String>,
    val annotatedMentionedPosts: List<AnnotatedMentionedPost>,
    val name: String,
    val isLikedByMe: Boolean,
    val isFollowedByMe: Boolean,
    val isOneself: Boolean,
    val trustScore: Float?,
    val numOfReplies: Int,
    val relays: List<String>,
) : Identifiable {
    override fun getId() = entity.id
}
