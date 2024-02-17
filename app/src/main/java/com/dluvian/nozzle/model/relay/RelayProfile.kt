package com.dluvian.nozzle.model.relay

import androidx.room.Embedded
import com.dluvian.nozzle.model.Pubkey
import com.google.gson.annotations.SerializedName

data class RelayProfile(
    val name: String = "",
    val description: String = "",
    val pubkey: Pubkey = "",
    @Embedded val limitation: RelayProfileLimitation = RelayProfileLimitation(),
    @SerializedName("payment_required") val paymentRequired: Boolean = false,
    @SerializedName("payments_url") val paymentsUrl: String = "",
    val software: String = "",
    val version: String = ""
)
