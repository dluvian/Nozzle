package com.dluvian.nozzle.ui.app.views.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun InboxRoute(
    inboxViewModel: InboxViewModel,
    profileFollower: IProfileFollower,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by inboxViewModel.uiState.collectAsState()
    val feedFlow by inboxViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()
    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedFeed = remember(forceFollowed, feed) {
        feed.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    InboxScreen(
        uiState = uiState,
        feed = adjustedFeed,
        postCardNavLambdas = postCardNavLambdas,
        onLike = { post ->
            inboxViewModel.postCardInteractor.like(
                scope = inboxViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onShowMedia = { mediaUrl ->
            inboxViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            inboxViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefresh = inboxViewModel.onRefresh,
        onLoadMore = inboxViewModel.onLoadMore,
        onPrepareReply = onPrepareReply,
        onFollow = { pubkeyToFollow ->
            profileFollower.follow(pubkeyToFollow = pubkeyToFollow)
        },
        onUnfollow = { pubkeyToUnfollow ->
            profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
        },
        onGoBack = onGoBack
    )
}
