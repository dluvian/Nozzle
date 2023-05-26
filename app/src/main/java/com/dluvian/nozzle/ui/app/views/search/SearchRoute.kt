package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SearchRoute(
    searchViewModel: SearchViewModel,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToThread: (String, String?, String?) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by searchViewModel.uiState.collectAsState()

    SearchScreen(
        uiState = uiState,
        onChangeInput = searchViewModel.onChangeInput,
        onValidateAndNavigateToDestination = {
            searchViewModel.onValidateAndNavigateToDestination(
                onNavigateToProfile,
                onNavigateToThread
            )
        },
        onResetUI = searchViewModel.onResetUI,
        onGoBack = onGoBack,
    )
}
