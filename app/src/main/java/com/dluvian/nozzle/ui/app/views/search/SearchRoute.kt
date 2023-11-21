package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SearchRoute(
    searchViewModel: SearchViewModel,
    onGoBack: () -> Unit,
) {
    val uiState by searchViewModel.uiState.collectAsState()

    SearchScreen(
        uiState = uiState,
        onSearch = searchViewModel.onSearch,
        onResetUI = searchViewModel.onResetUI,
        onGoBack = onGoBack,
    )
}
