package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.NO_ACTION
import com.dluvian.nozzle.model.nostr.Event

@Entity(
    tableName = "mention",
    primaryKeys = ["eventId", "pubkey"],
    foreignKeys = [androidx.room.ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = CASCADE,
        onUpdate = NO_ACTION
    )]
)
data class MentionEntity(
    val eventId: String,
    val pubkey: String,
) {
    companion object {
        fun fromEvent(event: Event): List<MentionEntity> {
            val mentions = event.getMentions()
            if (mentions.isEmpty()) return emptyList()

            return mentions.map { MentionEntity(eventId = event.id, pubkey = it) }
        }
    }
}
