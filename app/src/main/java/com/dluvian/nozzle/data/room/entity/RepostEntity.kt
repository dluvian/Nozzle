package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.dluvian.nozzle.model.EventId

@Entity(
    tableName = "repost",
    primaryKeys = ["eventId"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class RepostEntity(val eventId: EventId)
