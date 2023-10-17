package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "reaction",
    primaryKeys = ["eventId", "pubkey"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class ReactionEntity(
    val eventId: String,
    val pubkey: String,
)
