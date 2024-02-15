package com.dluvian.nozzle.model.relay

import com.dluvian.nozzle.model.Relay

data class RelaySelection(
    val relay: Relay,
    val isActive: Boolean,
)
