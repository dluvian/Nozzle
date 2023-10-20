package com.dluvian.nozzle.ui.app.views.addAccount

import androidx.compose.runtime.Composable

@Composable
fun AddAccountRoute(addAccountViewModel: AddAccountViewModel, onGoBack: () -> Unit) {
    AddAccountScreen(onGoBack = onGoBack)
}
