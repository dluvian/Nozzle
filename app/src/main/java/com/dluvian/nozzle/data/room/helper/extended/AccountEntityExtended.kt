package com.dluvian.nozzle.data.room.helper.extended

data class AccountEntityExtended(
    val pubkey: String,
    val isActive: Boolean,
    val name: String?,
    val picture: String?,
)
