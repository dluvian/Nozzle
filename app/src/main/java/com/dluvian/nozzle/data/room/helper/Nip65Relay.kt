package com.dluvian.nozzle.data.room.helper

import com.dluvian.nozzle.model.Relay

data class Nip65Relay(
    val url: Relay,
    val isRead: Boolean,
    val isWrite: Boolean,
)
