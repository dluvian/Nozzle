package com.dluvian.nozzle.ui.app.views.inbox

data class InboxViewModelState(
    val relays: List<String> = emptyList(),
    val isRefreshing: Boolean = false,
)
