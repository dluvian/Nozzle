package com.dluvian.nozzle.ui.app.views.relayProfile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun RelayProfileRoute(
    relayProfileViewModel: RelayProfileViewModel,
    onNavigateToProfile: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val relayProfile by relayProfileViewModel.relayProfile.collectAsState()
    val uiState by relayProfileViewModel.uiState.collectAsState()

    RelayProfileScreen(
        relayProfile = relayProfile,
        uiState = uiState,
        onRefresh = relayProfileViewModel.onRefresh,
        onAddToNip65 = relayProfileViewModel.onAddToNip65,
        onNavigateToProfile = onNavigateToProfile,
        onGoBack = onGoBack,
    )
}
