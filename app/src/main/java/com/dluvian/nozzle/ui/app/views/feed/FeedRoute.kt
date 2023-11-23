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
    onOpenDrawer: () -> Unit,
    onNavigateToPost: () -> Unit,
) {
    val uiState by feedViewModel.uiState.collectAsState()
    val metadataState by feedViewModel.metadataState.collectAsState()
    val pubkeyState by feedViewModel.pubkeyState.collectAsState()
    val feedFlow by feedViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    FeedScreen(
        uiState = uiState,
        pubkeyState = pubkeyState,
        feed = feed,
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
        onRefresh = feedViewModel.onRefresh,
        onRefreshOnMenuDismiss = feedViewModel.onRefreshOnMenuDismiss,
        onPrepareReply = onPrepareReply,
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
