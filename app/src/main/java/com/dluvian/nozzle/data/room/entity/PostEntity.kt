package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dluvian.nozzle.model.nostr.Event

@Entity(tableName = "post")
data class PostEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val pubkey: String,
    val replyToId: String?,
    val replyToRootId: String?,
    val replyRelayHint: String?,
    val repostedId: String?,
    val content: String,
    val createdAt: Long,
) {
    companion object {
        fun fromEvent(event: Event): PostEntity {
            return PostEntity(
                id = event.id,
                pubkey = event.pubkey,
                replyToId = event.getReplyId(),
                replyToRootId = event.getRootReplyId(),
                replyRelayHint = event.getReplyRelayHint(),
                repostedId = event.getRepostedId(),
                content = event.content,
                createdAt = event.createdAt,
            )
        }
    }
}
