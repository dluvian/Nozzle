package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dluvian.nozzle.model.nostr.Event

@Entity(
    tableName = "post",
    indices = [Index(value = ["id"]), Index(value = ["createdAt"]), Index(value = ["content"])]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val pubkey: String,
    val replyToId: String?,
    val replyRelayHint: String?,
    val content: String,
    val createdAt: Long,
) {
    companion object {
        fun fromEvent(event: Event): PostEntity {
            return PostEntity(
                id = event.id,
                pubkey = event.pubkey,
                replyToId = event.getReplyId(),
                replyRelayHint = event.getReplyRelayHint(),
                content = event.content.trim(),
                createdAt = event.createdAt,
            )
        }
    }
}
