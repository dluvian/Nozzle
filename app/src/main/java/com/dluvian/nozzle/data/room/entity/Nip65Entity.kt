package com.dluvian.nozzle.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import com.dluvian.nozzle.data.room.helper.Nip65Relay

@Entity(tableName = "nip65", primaryKeys = ["pubkey", "url"])
data class Nip65Entity(
    val pubkey: String,
    @Embedded val nip65Relay: Nip65Relay,
    val createdAt: Long,
)
