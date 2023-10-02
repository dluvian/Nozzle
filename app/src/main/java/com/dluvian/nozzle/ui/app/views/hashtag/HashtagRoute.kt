package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.PostWithMeta

@Composable
fun HashtagRoute(
    hashtagViewModel: HashtagViewModel,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToThread: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
    onNavigateToId: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by hashtagViewModel.uiState.collectAsState()
    val feedState by hashtagViewModel.feedState.collectAsState()

    HashtagScreen(
        uiState = uiState,
        feed = feedState,
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
        onPrepareReply = onPrepareReply,
        onNavigateToThread = onNavigateToThread,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToReply = onNavigateToReply,
        onNavigateToQuote = onNavigateToQuote,
        onNavigateToId = onNavigateToId,
        onGoBack = onGoBack
    )
}
