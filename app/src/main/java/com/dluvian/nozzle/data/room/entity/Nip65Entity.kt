package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity

@Entity(tableName = "nip65", primaryKeys = ["pubkey", "url"])
data class Nip65Entity(
    val pubkey: String,
    val url: String,
    val isRead: Boolean,
    val isWrite: Boolean,
    val createdAt: Long,
)
