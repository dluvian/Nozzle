package com.dluvian.nozzle.data.room.helper.extended

import androidx.room.Embedded
import com.dluvian.nozzle.data.room.entity.PostEntity

data class PostEntityExtended(
    @Embedded
    val postEntity: PostEntity,
    val replyToPubkey: String?,
    val replyToName: String?,
    val name: String?,
    val picture: String?,
    val numOfReplies: Int,
    val isLikedByMe: Boolean,
)
