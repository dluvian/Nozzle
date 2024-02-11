package com.dluvian.nozzle.ui.app.views.likes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

@Composable
fun LikesRoute(
    likesViewModel: LikesViewModel,
    showProfilePicture: Boolean,
    profileFollower: IProfileFollower,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val isRefreshing by likesViewModel.isRefreshing.collectAsState()
    val likeCount by likesViewModel.likeCount.collectAsState()

    val feedFlow by likesViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    val numOfNewPostsFlow by likesViewModel.numOfNewPosts.collectAsState()
    val numOfNewPosts by numOfNewPostsFlow.collectAsState()

    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedFeed = remember(forceFollowed, feed) {
        feed.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    LikesScreen(
        feed = adjustedFeed,
        showProfilePicture = showProfilePicture,
        numOfNewPosts = numOfNewPosts,
        likeCount = likeCount,
        isRefreshing = isRefreshing,
        postCardLambdas = postCardLambdas,
        onRefresh = likesViewModel.onRefresh,
        onLoadMore = likesViewModel.onLoadMore,
        onPrepareReply = onPrepareReply,
        onGoBack = onGoBack
    )
}
