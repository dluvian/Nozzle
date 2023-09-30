package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity

@Entity(tableName = "hashtag", primaryKeys = ["eventId", "hashtag"])
data class HashtagEntity(
    val eventId: String,
    val hashtag: String,
)
