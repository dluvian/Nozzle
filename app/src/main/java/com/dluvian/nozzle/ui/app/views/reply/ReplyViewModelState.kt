package com.dluvian.nozzle.ui.app.views.reply

import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.SimpleProfile

data class ReplyViewModelState(
    val recipientName: String = "",
    val relaySelection: List<RelayActive> = emptyList(),
    val searchSuggestions: List<SimpleProfile> = emptyList(),
)
