package com.dluvian.nozzle.ui.app.views.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

// TODO: Nav with args, no need for onPrepare
@Composable
fun FeedRoute(
    feedViewModel: FeedViewModel,
    profileFollower: IProfileFollower,
    postCardInteractor: IPostCardInteractor,
    mediaCache: IClickedMediaUrlCache,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToPost: () -> Unit,
) {
    val uiState by feedViewModel.uiState.collectAsState()
    val pubkey by feedViewModel.pubkeyState.collectAsState()
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
        postCardNavLambdas = postCardNavLambdas,
        onLike = { post ->
            postCardInteractor.like(
                scope = feedViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onShowMedia = { mediaUrl -> mediaCache.insert(mediaUrl) },
        onShouldShowMedia = { mediaUrl -> mediaCache.contains(mediaUrl) },
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
        onFollow = { pubkeyToFollow ->
            profileFollower.follow(pubkeyToFollow = pubkeyToFollow)
        },
        onUnfollow = { pubkeyToUnfollow ->
            profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
        },
        onNavigateToPost = onNavigateToPost,
    )
}
