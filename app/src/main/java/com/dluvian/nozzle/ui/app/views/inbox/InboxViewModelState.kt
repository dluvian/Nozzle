package com.dluvian.nozzle.ui.app.views.inbox

import androidx.compose.runtime.Immutable

@Immutable
data class InboxViewModelState(
    val relays: List<String> = emptyList(),
    val isRefreshing: Boolean = false,
)
