package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity

@Entity(tableName = "reaction", primaryKeys = ["eventId", "pubkey"])
data class ReactionEntity(
    val eventId: String,
    val pubkey: String,
)
