package com.dluvian.nozzle.ui.app.views.reply

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.model.RelaySelection
import com.dluvian.nozzle.model.SimpleProfile

@Immutable
data class ReplyViewModelState(
    val recipientName: String = "",
    val relaySelection: List<RelaySelection> = emptyList(),
    val searchSuggestions: List<SimpleProfile> = emptyList(),
)
