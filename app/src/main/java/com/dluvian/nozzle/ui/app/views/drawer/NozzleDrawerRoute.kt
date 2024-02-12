package com.dluvian.nozzle.ui.app.views.drawer


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.app.navigation.NozzleNavActions

@Composable
fun NozzleDrawerRoute(
    nozzleDrawerViewModel: NozzleDrawerViewModel,
    showProfilePicture: Boolean,
    navActions: NozzleNavActions,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by nozzleDrawerViewModel.uiState.collectAsState()

    NozzleDrawerScreen(
        modifier = modifier,
        uiState = uiState,
        showProfilePicture = showProfilePicture,
        navActions = navActions,
        onActivateAccount = nozzleDrawerViewModel.onActivateAccount,
        onDeleteAccount = nozzleDrawerViewModel.onDeleteAccount,
        closeDrawer = closeDrawer,
    )
}
