package com.dluvian.nozzle.ui.app.views.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.media3.exoplayer.ExoPlayer
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

// TODO: Nav with args, no need for onPrepare
@Composable
fun FeedRoute(
    feedViewModel: FeedViewModel,
    videoPlayer: ExoPlayer,
    profileFollower: IProfileFollower,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToPost: () -> Unit,
) {
    val uiState by feedViewModel.uiState.collectAsState()
    val pubkey by feedViewModel.pubkeyState.collectAsState()

    val numOfNewPostsFlow by feedViewModel.numOfNewPosts.collectAsState()
    val numOfNewPosts by numOfNewPostsFlow.collectAsState()

    val feedFlow by feedViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedFeed = remember(forceFollowed, feed) {
        feed.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    FeedScreen(
        uiState = uiState,
        pubkey = pubkey,
        feed = adjustedFeed,
        numOfNewPosts = numOfNewPosts,
        videoPlayer = videoPlayer,
        postCardLambdas = postCardLambdas,
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
