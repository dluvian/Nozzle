package com.dluvian.nozzle.model.relay

import com.google.gson.annotations.SerializedName

data class RelayProfileLimitation(
    @SerializedName("payment_required") val paymentRequired: Boolean? = null,
    @SerializedName("auth_required") val authRequired: Boolean? = null,
    @SerializedName("restricted_writes") val restrictedWrites: Boolean? = null,
)
