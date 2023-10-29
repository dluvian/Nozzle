package com.dluvian.nozzle.ui.app.views.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun PostRoute(
    postViewModel: PostViewModel,
    onGoBack: () -> Unit,
) {
    val uiState by postViewModel.uiState.collectAsState()
    val metadataState by postViewModel.metadataState.collectAsState()
    val pubkeyState by postViewModel.pubkeyState.collectAsState()

    PostScreen(
        uiState = uiState,
        metadataState = metadataState,
        pubkeyState = pubkeyState,
        onToggleRelaySelection = postViewModel.onToggleRelaySelection,
        onSend = postViewModel.onSend,
        onGoBack = onGoBack,
    )
}
