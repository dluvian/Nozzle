package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey

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
)
