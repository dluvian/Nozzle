package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity

@Entity(tableName = "contact", primaryKeys = ["pubkey", "contactPubkey"])
data class ContactEntity(
    val pubkey: String,
    val contactPubkey: String,
    val createdAt: Long,
)
// TODO: Add relayUrl again. Autopilot could use hint from contactlist when no event relay found
