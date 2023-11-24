package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SearchRoute(
    searchViewModel: SearchViewModel,
    onNavigateToId: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by searchViewModel.uiState.collectAsState()
    val profileSearchResult by searchViewModel.profileSearchResult.collectAsState()

    SearchScreen(
        uiState = uiState,
        profileSearchResult = profileSearchResult,
        onSearch = searchViewModel.onSearch,
        onResetUI = searchViewModel.onResetUI,
        onNavigateToId = onNavigateToId,
        onNavigateToProfile = onNavigateToProfile,
        onGoBack = onGoBack,
    )
}
