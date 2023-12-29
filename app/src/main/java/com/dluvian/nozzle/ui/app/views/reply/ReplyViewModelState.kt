package com.dluvian.nozzle.ui.app.views.reply

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.SimpleProfile

@Immutable
data class ReplyViewModelState(
    val recipientName: String = "",
    val relaySelection: List<RelayActive> = emptyList(),
    val searchSuggestions: List<SimpleProfile> = emptyList(),
)
