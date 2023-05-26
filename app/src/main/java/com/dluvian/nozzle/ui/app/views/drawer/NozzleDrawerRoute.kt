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
    val pubkeyState by nozzleDrawerViewModel.pubkeyState.collectAsState()
    val metadataState by nozzleDrawerViewModel.metadataState.collectAsState()

    NozzleDrawerScreen(
        modifier = modifier,
        pubkeyState = pubkeyState,
        metadataState = metadataState,
        navActions = navActions,
        closeDrawer = closeDrawer,
    )
}
