package com.dluvian.nozzle.ui.app.views.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold

@Composable
fun SettingsScreen(
    uiState: SettingsViewModelState,
    onGoBack: () -> Unit,
) {
    ReturnableScaffold(
        topBarText = stringResource(id = R.string.settings),
        onGoBack = onGoBack,
    ) {
        Text("YEET")
    }
}
