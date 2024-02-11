package com.dluvian.nozzle.ui.app.views.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

@Composable
fun ThreadRoute(
    threadViewModel: ThreadViewModel,
    showProfilePicture: Boolean,
    profileFollower: IProfileFollower,
    postCardLambdas: PostCardLambdas,
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
        showProfilePicture = showProfilePicture,
        isRefreshing = isRefreshing,
        postCardLambdas = postCardLambdas,
        onPrepareReply = onPrepareReply,
        onRefreshThreadView = threadViewModel.onRefreshThreadView,
        onFindPrevious = threadViewModel.onFindPrevious,
        onGoBack = onGoBack,
    )
}
