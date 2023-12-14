package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun SearchRoute(
    searchViewModel: SearchViewModel,
    postCardInteractor: IPostCardInteractor,
    profileFollower: IProfileFollower,
    mediaCache: IClickedMediaUrlCache,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToId: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by searchViewModel.uiState.collectAsState()
    val profileSearchResult by searchViewModel.profileSearchResult.collectAsState()
    val postSearchResult by searchViewModel.postSearchResult.collectAsState()

    SearchScreen(
        uiState = uiState,
        profileSearchResult = profileSearchResult,
        postSearchResult = postSearchResult,
        onManualSearch = searchViewModel.onManualSearch,
        onTypeSearch = searchViewModel.onTypeSearch,
        onChangeSearchType = searchViewModel.onChangeSearchType,
        onResetUI = searchViewModel.onResetUI,
        onSubscribeUnknownContacts = searchViewModel.onSubscribeUnknownContacts,
        onNavigateToId = onNavigateToId,
        onNavigateToProfile = onNavigateToProfile,
        postCardNavLambdas = postCardNavLambdas,
        onPrepareReply = onPrepareReply,
        onShowMedia = { mediaUrl -> mediaCache.insert(mediaUrl) },
        onShouldShowMedia = { mediaUrl -> mediaCache.contains(mediaUrl) },
        onFollow = { pubkeyToFollow ->
            profileFollower.follow(pubkeyToFollow = pubkeyToFollow)
        },
        onUnfollow = { pubkeyToUnfollow ->
            profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
        },
        onLike = { post ->
            postCardInteractor.like(
                scope = searchViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onGoBack = onGoBack,
    )
}
