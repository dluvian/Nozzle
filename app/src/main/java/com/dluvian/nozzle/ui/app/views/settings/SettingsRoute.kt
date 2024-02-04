package com.dluvian.nozzle.ui.app.views.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SettingsRoute(settingsViewModel: SettingsViewModel, onGoBack: () -> Unit) {
    val uiState by settingsViewModel.uiState.collectAsState()

    SettingsScreen(
        uiState = uiState,
        onGoBack = onGoBack
    )
}
