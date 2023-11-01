package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.nozzle.model.nostr.Event

@Entity(
    tableName = "hashtag",
    primaryKeys = ["eventId", "hashtag"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class HashtagEntity(
    val eventId: String,
    val hashtag: String,
) {
    companion object {
        fun fromEvent(event: Event): List<HashtagEntity> {
            val hashtags = event.getHashtags()
            if (hashtags.isEmpty()) return emptyList()

            return hashtags.map { HashtagEntity(eventId = event.id, hashtag = it) }
        }
    }
}
