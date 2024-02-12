package com.dluvian.nozzle.ui.app.views.settings

import androidx.compose.runtime.Composable

@Composable
fun SettingsRoute(settingsViewModel: SettingsViewModel, onGoBack: () -> Unit) {

    SettingsScreen(
        preferenceStates = settingsViewModel.settingsPreferenceStates,
        onToggleShowProfilePictures = settingsViewModel.onToggleShowProfilePictures,
        onToggleDarkMode = settingsViewModel.onToggleDarkMode,
        onGoBack = onGoBack
    )
}
