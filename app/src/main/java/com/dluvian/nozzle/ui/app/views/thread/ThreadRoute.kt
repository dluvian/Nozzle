package com.dluvian.nozzle.ui.app.views.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun ThreadRoute(
    threadViewModel: ThreadViewModel,
    profileFollower: IProfileFollower,
    postCardInteractor: IPostCardInteractor,
    mediaCache: IClickedMediaUrlCache,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val thread by threadViewModel.threadState.collectAsState()
    val isRefreshing by threadViewModel.isRefreshingState.collectAsState()
    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedThread = remember(thread, forceFollowed) {
        val current = thread.current?.let {
            it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe)
        }
        val previous = thread.previous.map {
            it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe)
        }
        val replies = thread.replies.map {
            it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe)
        }
        PostThread(current = current, previous = previous, replies = replies)
    }

    ThreadScreen(
        thread = adjustedThread,
        isRefreshing = isRefreshing,
        postCardNavLambdas = postCardNavLambdas,
        onPrepareReply = onPrepareReply,
        onLike = { post ->
            postCardInteractor.like(
                scope = threadViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onRefreshThreadView = threadViewModel.onRefreshThreadView,
        onFindPrevious = threadViewModel.onFindPrevious,
        onShowMedia = { mediaUrl -> mediaCache.insert(mediaUrl) },
        onShouldShowMedia = { mediaUrl -> mediaCache.contains(mediaUrl) },
        onFollow = { pubkeyToFollow ->
            profileFollower.follow(pubkeyToFollow = pubkeyToFollow)
        },
        onUnfollow = { pubkeyToUnfollow ->
            profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
        },
        onGoBack = onGoBack,
    )
}
