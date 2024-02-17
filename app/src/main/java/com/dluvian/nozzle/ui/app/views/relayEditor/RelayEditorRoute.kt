package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dluvian.nozzle.model.Relay

@Composable
fun RelayEditorRoute(
    relayEditorViewModel: RelayEditorViewModel,
    onOpenRelayProfile: (Relay) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by relayEditorViewModel.uiState.collectAsState()
    val onlineStatuses by relayEditorViewModel.onlineStatuses.collectAsState()

    RelayEditorScreen(
        uiState = uiState,
        onlineStatuses = onlineStatuses,
        onSaveRelays = relayEditorViewModel.onSaveRelays,
        onAddRelay = relayEditorViewModel.onAddRelay,
        onDeleteRelay = relayEditorViewModel.onDeleteRelay,
        onToggleRead = relayEditorViewModel.onToggleRead,
        onToggleWrite = relayEditorViewModel.onToggleWrite,
        onUsePopularRelay = relayEditorViewModel.onUsePopularRelay,
        onOpenRelayProfile = onOpenRelayProfile,
        onGoBack = onGoBack,
    )
}
