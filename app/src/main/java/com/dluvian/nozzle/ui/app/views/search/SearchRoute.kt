package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SearchRoute(
    searchViewModel: SearchViewModel,
    onNavigateToId: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by searchViewModel.uiState.collectAsState()

    SearchScreen(
        uiState = uiState,
        onChangeInput = searchViewModel.onChangeInput,
        onSearch = searchViewModel.onSearch,
        onNavigateToId = onNavigateToId,
        onResetUI = searchViewModel.onResetUI,
        onGoBack = onGoBack,
    )
}
