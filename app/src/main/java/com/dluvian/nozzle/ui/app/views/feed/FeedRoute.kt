package com.dluvian.nozzle.ui.app.views.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dluvian.nozzle.model.PostWithMeta

// TODO: Nav with args, no need for onPrepare
@Composable
fun FeedRoute(
    feedViewModel: FeedViewModel,
    onPrepareReply: (PostWithMeta) -> Unit,
    onPreparePost: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToThread: (String, String?) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToPost: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
) {
    val uiState by feedViewModel.uiState.collectAsState()
    val metadataState by feedViewModel.metadataState.collectAsState()
    val feedState by feedViewModel.feedState.collectAsState()

    FeedScreen(
        uiState = uiState,
        feedState = feedState,
        metadataState = metadataState,
        onLike = { post ->
            feedViewModel.postCardInteractor.like(postId = post.id, postPubkey = post.pubkey)
        },
        onShowMedia = { mediaUrl ->
            feedViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            feedViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onPrepareReply = onPrepareReply,
        onPreparePost = onPreparePost,
        onToggleContactsOnly = feedViewModel.onToggleContactsOnly,
        onTogglePosts = feedViewModel.onTogglePosts,
        onToggleReplies = feedViewModel.onToggleReplies,
        onToggleRelayIndex = feedViewModel.onToggleRelayIndex,
        onToggleAutopilot = feedViewModel.onToggleAutopilot,
        onRefreshFeedView = feedViewModel.onRefreshFeedView,
        onRefreshOnMenuDismiss = feedViewModel.onRefreshOnMenuDismiss,
        onLoadMore = feedViewModel.onLoadMore,
        onOpenDrawer = onOpenDrawer,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToThread = { postIds ->
            onNavigateToThread(
                postIds.id,
                postIds.replyToId,
            )
        },
        onNavigateToReply = onNavigateToReply,
        onNavigateToPost = onNavigateToPost,
        onNavigateToQuote = onNavigateToQuote,
    )
}
