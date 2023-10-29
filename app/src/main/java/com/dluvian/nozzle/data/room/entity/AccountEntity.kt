package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity

@Entity(tableName = "account", primaryKeys = ["pubkey"])
data class AccountEntity(
    val pubkey: String,
    val isActive: Boolean = false,
)
