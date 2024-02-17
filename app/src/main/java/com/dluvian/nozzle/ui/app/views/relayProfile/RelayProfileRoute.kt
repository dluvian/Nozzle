package com.dluvian.nozzle.ui.app.views.relayProfile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun RelayProfileRoute(
    relayProfileViewModel: RelayProfileViewModel,
    onGoBack: () -> Unit,
) {
    val relayProfile by relayProfileViewModel.relayProfile.collectAsState()
    val relay by relayProfileViewModel.currentRelay
    val isRefreshing by relayProfileViewModel.isRefreshing.collectAsState()

    RelayProfileScreen(
        relayProfile = relayProfile,
        relay = relay,
        isRefreshing = isRefreshing,
        onRefresh = relayProfileViewModel.onRefresh,
        onGoBack = onGoBack,
    )
}
