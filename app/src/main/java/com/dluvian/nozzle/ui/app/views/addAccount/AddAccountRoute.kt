package com.dluvian.nozzle.ui.app.views.addAccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun AddAccountRoute(
    addAccountViewModel: AddAccountViewModel,
    onGoBack: () -> Unit
) {
    val uiState by addAccountViewModel.uiState.collectAsState()

    AddAccountScreen(
        uiState = uiState,
        onGenerateNew = addAccountViewModel.onGenerateNew,
        onLogin = { nsec ->
            val success = addAccountViewModel.onLogin(nsec)
            if (success) {
                onGoBack()
            }
        },
        onGoBack = onGoBack
    )
}
