package com.dluvian.nozzle.ui.app.views.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun PostRoute(
    postViewModel: PostViewModel,
    showProfilePicture: Boolean,
    onGoBack: () -> Unit,
) {
    val uiState by postViewModel.uiState.collectAsState()
    val pubkey by postViewModel.pubkeyState.collectAsState()
    val picture by postViewModel.pictureState.collectAsState()

    PostScreen(
        uiState = uiState,
        pubkey = pubkey,
        picture = picture,
        showProfilePicture = showProfilePicture,
        onToggleRelaySelection = postViewModel.onToggleRelaySelection,
        onSend = { content -> postViewModel.onSend(content, uiState) },
        onSearch = postViewModel.onSearch,
        onClickMention = postViewModel.onClickMention,
        onGoBack = onGoBack,
    )
}
