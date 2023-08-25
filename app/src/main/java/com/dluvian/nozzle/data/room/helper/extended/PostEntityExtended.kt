package com.dluvian.nozzle.data.room.helper.extended

import androidx.room.Embedded
import com.dluvian.nozzle.data.room.entity.PostEntity

data class PostEntityExtended(
    @Embedded
    val postEntity: PostEntity,
    val replyToPubkey: String?,
    val replyToName: String?,
    val name: String?,
    val pictureUrl: String?,
    val numOfReplies: Int,
    val isLikedByMe: Boolean,
    val mentionedPostPubkey: String?,
    val mentionedPostContent: String?,
    val mentionedPostName: String?,
    val mentionedPostPicture: String?,
    val mentionedPostCreatedAt: Long?,
)
