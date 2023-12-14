package com.dluvian.nozzle.ui.app.views.hashtag

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

@Composable
fun HashtagRoute(
    hashtagViewModel: HashtagViewModel,
    profileFollower: IProfileFollower,
    mediaCache: IClickedMediaUrlCache,
    postCardInteractor: IPostCardInteractor,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by hashtagViewModel.uiState.collectAsState()
    val feedFlow by hashtagViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()
    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedFeed = remember(forceFollowed, feed) {
        feed.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    HashtagScreen(
        uiState = uiState,
        feed = adjustedFeed,
        postCardNavLambdas = postCardNavLambdas,
        onLike = { post ->
            postCardInteractor.like(
                scope = hashtagViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onShowMedia = { mediaUrl -> mediaCache.insert(mediaUrl) },
        onShouldShowMedia = { mediaUrl -> mediaCache.contains(mediaUrl) },
        onRefresh = hashtagViewModel.onRefresh,
        onLoadMore = hashtagViewModel.onLoadMore,
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
