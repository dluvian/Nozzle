package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dluvian.nozzle.model.nostr.Event

@Entity(tableName = "post")
data class PostEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val pubkey: String,
    // TODO: Use @Embedded for composition
    val replyToId: String?,
    val replyToRootId: String?,
    val replyRelayHint: String?,
    val mentionedPostId: String?, // TODO: Multiple posts
    val mediaUrl: String?, // TODO: Multiple pictures
    val content: String,
    val createdAt: Long,
) {
    companion object {
        fun fromEvent(event: Event): PostEntity {
            val contentContext = event.parseContent()
            return PostEntity(
                id = event.id,
                pubkey = event.pubkey,
                replyToId = event.getReplyId(),
                replyToRootId = event.getRootReplyId(),
                replyRelayHint = event.getReplyRelayHint(),
                mentionedPostId = contentContext.mentionedPostId,
                mediaUrl = contentContext.mediaUrl,
                content = contentContext.cleanContent,
                createdAt = event.createdAt,
            )
        }
    }
}
