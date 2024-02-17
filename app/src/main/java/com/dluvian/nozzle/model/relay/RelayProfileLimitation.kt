package com.dluvian.nozzle.model.relay

import com.google.gson.annotations.SerializedName

data class RelayProfileLimitation(
    @SerializedName("auth_required") val authRequired: Boolean = false,
    @SerializedName("restricted_writes") val restrictedWrites: Boolean = false,
)
