package com.dluvian.nozzle.ui.app.views.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ReplyRoute(
    replyViewModel: ReplyViewModel,
    onGoBack: () -> Unit,
) {
    val uiState by replyViewModel.uiState.collectAsState()
    val metadataState by replyViewModel.metadataState.collectAsState()
    val pubkeyState by replyViewModel.pubkeyState.collectAsState()

    ReplyScreen(
        uiState = uiState,
        metadataState = metadataState,
        pubkeyState = pubkeyState,
        onToggleRelaySelection = replyViewModel.onToggleRelaySelection,
        onSend = replyViewModel.onSend,
        onGoBack = onGoBack,
    )
}
