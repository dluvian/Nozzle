package com.dluvian.nozzle.ui.app.views.drawer


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.app.navigation.NozzleNavActions

@Composable
fun NozzleDrawerRoute(
    nozzleDrawerViewModel: NozzleDrawerViewModel,
    navActions: NozzleNavActions,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by nozzleDrawerViewModel.uiState.collectAsState()
    val isDarkMode by nozzleDrawerViewModel.isDarkMode

    NozzleDrawerScreen(
        modifier = modifier,
        uiState = uiState,
        isDarkMode = isDarkMode,
        navActions = navActions,
        onActivateAccount = nozzleDrawerViewModel.onActivateAccount,
        onDeleteAccount = nozzleDrawerViewModel.onDeleteAccount,
        onToggleDarkMode = nozzleDrawerViewModel.onToggleDarkMode,
        closeDrawer = closeDrawer,
    )
}
