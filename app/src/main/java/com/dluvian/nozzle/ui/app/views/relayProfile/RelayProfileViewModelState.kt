package com.dluvian.nozzle.ui.app.views.relayProfile

import com.dluvian.nozzle.model.Relay

data class RelayProfileViewModelState(
    val relay: Relay = "",
    val isRefreshing: Boolean = false,
    val isAddableToNip65: Boolean = false,
    val isUpdatingNip65: Boolean = false,
)
