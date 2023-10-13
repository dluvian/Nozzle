package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity

@Entity(tableName = "mention", primaryKeys = ["eventId", "pubkey"])
data class MentionEntity(
    val eventId: String,
    val pubkey: String,
)
