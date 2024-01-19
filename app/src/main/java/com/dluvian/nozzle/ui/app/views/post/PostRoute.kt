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
    val pubkeyState by postViewModel.pubkeyState.collectAsState()

    PostScreen(
        uiState = uiState,
        pubkey = pubkeyState,
        onToggleRelaySelection = postViewModel.onToggleRelaySelection,
        onSend = { content -> postViewModel.onSend(content, uiState) },
        onSearch = postViewModel.onSearch,
        onClickMention = postViewModel.onClickMention,
        onGoBack = onGoBack,
    )
}
