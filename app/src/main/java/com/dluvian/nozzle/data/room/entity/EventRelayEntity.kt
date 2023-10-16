package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.NO_ACTION

@Entity(
    tableName = "eventRelay",
    primaryKeys = ["eventId", "relayUrl"],
    foreignKeys = [ForeignKey(
        entity = PostEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = NO_ACTION
    )]
)
data class EventRelayEntity(
    val eventId: String,
    val relayUrl: String,
)
