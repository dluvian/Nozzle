package com.dluvian.nozzle.ui.app.views.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

// TODO: Nav with args, no need for onPrepare
@Composable
fun FeedRoute(
    feedViewModel: FeedViewModel,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onPreparePost: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToPost: () -> Unit,
) {
    val uiState by feedViewModel.uiState.collectAsState()
    val metadataState by feedViewModel.metadataState.collectAsState()
    val feedState by feedViewModel.feedState.collectAsState()

    FeedScreen(
        uiState = uiState,
        feedState = feedState,
        metadataState = metadataState,
        postCardNavLambdas = postCardNavLambdas,
        onLike = { post ->
            feedViewModel.postCardInteractor.like(
                scope = feedViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onShowMedia = { mediaUrl ->
            feedViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            feedViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefreshFeedView = feedViewModel.onRefreshFeedView,
        onRefreshOnMenuDismiss = feedViewModel.onRefreshOnMenuDismiss,
        onPrepareReply = onPrepareReply,
        onPreparePost = onPreparePost,
        onToggleContactsOnly = feedViewModel.onToggleContactsOnly,
        onTogglePosts = feedViewModel.onTogglePosts,
        onToggleReplies = feedViewModel.onToggleReplies,
        onToggleRelayIndex = feedViewModel.onToggleRelayIndex,
        onToggleAutopilot = feedViewModel.onToggleAutopilot,
        onLoadMore = feedViewModel.onLoadMore,
        onOpenDrawer = onOpenDrawer,
        onNavigateToPost = onNavigateToPost,
    )
}
