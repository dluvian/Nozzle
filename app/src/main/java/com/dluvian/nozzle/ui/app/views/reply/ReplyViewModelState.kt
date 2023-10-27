package com.dluvian.nozzle.ui.app.views.reply

import com.dluvian.nozzle.model.RelayActive

data class ReplyViewModelState(
    val recipientName: String = "",
    val reply: String = "",
    val isSendable: Boolean = false,
    val relaySelection: List<RelayActive> = emptyList(),
)
