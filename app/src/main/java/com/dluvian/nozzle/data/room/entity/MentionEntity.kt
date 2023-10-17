package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.NO_ACTION

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
)
