package com.dluvian.nozzle.model.relay

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import com.dluvian.nozzle.model.Pubkey
import com.google.gson.annotations.SerializedName

@Immutable
data class RelayProfile(
    val name: String? = null,
    val description: String? = null,
    val pubkey: Pubkey? = null,
    @Embedded val limitation: RelayProfileLimitation? = null,
    @SerializedName("payments_url") val paymentsUrl: String? = null,
    val software: String? = null,
    val version: String? = null
)
