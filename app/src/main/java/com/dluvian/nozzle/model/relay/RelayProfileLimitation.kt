package com.dluvian.nozzle.model.relay

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class RelayProfileLimitation(
    @SerializedName("payment_required") val paymentRequired: Boolean? = null,
    @SerializedName("auth_required") val authRequired: Boolean? = null,
    @SerializedName("restricted_writes") val restrictedWrites: Boolean? = null,
)
