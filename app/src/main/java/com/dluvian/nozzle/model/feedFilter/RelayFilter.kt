package com.dluvian.nozzle.model.feedFilter

import com.dluvian.nozzle.model.Relay

sealed class RelayFilter
data object Autopilot : RelayFilter()
data object ReadRelays : RelayFilter()
data class MultipleRelays(val relays: List<Relay>) : RelayFilter()
