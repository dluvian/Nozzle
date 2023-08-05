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
    val replyRelayHint: String?,
    val mentionedPostId: String?,
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
                replyRelayHint = event.getReplyRelayHint(),
                mentionedPostId = contentContext.mentionedPostId,
                content = contentContext.cleanContent,
                createdAt = event.createdAt,
            )
        }
    }
}
