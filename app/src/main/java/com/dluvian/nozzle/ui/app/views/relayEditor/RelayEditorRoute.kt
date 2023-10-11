package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun RelayEditorRoute(
    relayEditorViewModel: RelayEditorViewModel,
    onGoBack: () -> Unit,
) {
    val uiState by relayEditorViewModel.uiState.collectAsState()

    RelayEditorScreen(
        uiState = uiState,
        onSaveRelays = relayEditorViewModel.onSaveRelays,
        onAddRelay = relayEditorViewModel.onAddRelay,
        onDeleteRelay = relayEditorViewModel.onDeleteRelay,
        onGoBack = onGoBack,
    )
}
