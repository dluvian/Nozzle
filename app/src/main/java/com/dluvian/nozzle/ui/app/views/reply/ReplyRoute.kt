package com.dluvian.nozzle.ui.app.views.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ReplyRoute(
    replyViewModel: ReplyViewModel,
    showProfilePicture: Boolean,
    onGoBack: () -> Unit,
) {
    val uiState by replyViewModel.uiState.collectAsState()
    val pubkey by replyViewModel.pubkeyState.collectAsState()
    val picture by replyViewModel.pictureState.collectAsState()

    ReplyScreen(
        uiState = uiState,
        showProfilePicture = showProfilePicture,
        pubkey = pubkey,
        picture = picture,
        onToggleRelaySelection = replyViewModel.onToggleRelaySelection,
        onSend = replyViewModel.onSend,
        onSearch = replyViewModel.onSearch,
        onClickMention = replyViewModel.onClickMention,
        onGoBack = onGoBack,
    )
}
