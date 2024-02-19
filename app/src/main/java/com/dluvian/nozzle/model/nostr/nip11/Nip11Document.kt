package com.dluvian.nozzle.model.nostr.nip11

import androidx.room.Embedded
import com.dluvian.nozzle.model.relay.RelayProfileLimitation
import com.google.gson.annotations.SerializedName

data class Nip11Document(
    val name: String?,
    val description: String?,
    val pubkey: String?,
    @Embedded val limitation: RelayProfileLimitation?,
    @SerializedName("payments_url") val paymentsUrl: String?,
    val software: String?,
    val version: String?
)
