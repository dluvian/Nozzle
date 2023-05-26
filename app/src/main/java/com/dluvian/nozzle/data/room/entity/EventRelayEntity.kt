package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity

@Entity(tableName = "eventRelay", primaryKeys = ["eventId", "relayUrl"])
data class EventRelayEntity(
    val eventId: String,
    val relayUrl: String,
)
