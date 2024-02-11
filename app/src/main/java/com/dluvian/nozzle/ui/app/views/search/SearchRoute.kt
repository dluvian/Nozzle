package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

@Composable
fun SearchRoute(
    searchViewModel: SearchViewModel,
    showProfilePicture: Boolean,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by searchViewModel.uiState.collectAsState()
    val profileSearchResult by searchViewModel.profileSearchResult.collectAsState()
    val postSearchResult by searchViewModel.postSearchResult.collectAsState()

    SearchScreen(
        uiState = uiState,
        showProfilePicture = showProfilePicture,
        postCardLambdas = postCardLambdas,
        profileSearchResult = profileSearchResult,
        postSearchResult = postSearchResult,
        onManualSearch = searchViewModel.onManualSearch,
        onTypeSearch = searchViewModel.onTypeSearch,
        onChangeSearchType = searchViewModel.onChangeSearchType,
        onResetUI = searchViewModel.onResetUI,
        onSubscribeUnknownContacts = searchViewModel.onSubscribeUnknownContacts,
        onPrepareReply = onPrepareReply,
        onGoBack = onGoBack,
    )
}
