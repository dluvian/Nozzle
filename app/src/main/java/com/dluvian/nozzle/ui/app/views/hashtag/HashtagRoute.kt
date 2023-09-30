package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope

@Composable
fun HashtagRoute(
    hashtagViewModel: HashtagViewModel,
    onNavigateToThread: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReply: (String) -> Unit,
    onNavigateToQuote: (String) -> Unit,
    onNavigateToId: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by hashtagViewModel.uiState.collectAsState()
    val feedState by hashtagViewModel.feedState.collectAsState()

    HashtagScreen(
        uiState, feedState,
        onLike = { post ->
            hashtagViewModel.postCardInteractor.like(
                scope = hashtagViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onShowMedia = { mediaUrl ->
            hashtagViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            hashtagViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefresh = hashtagViewModel.onRefresh,
        onLoadMore = hashtagViewModel.onLoadMore,
        onNavigateToThread = onNavigateToThread,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToReply = onNavigateToReply,
        onNavigateToQuote = onNavigateToQuote,
        onNavigateToId = onNavigateToId,
        onGoBack = onGoBack
    )
}
