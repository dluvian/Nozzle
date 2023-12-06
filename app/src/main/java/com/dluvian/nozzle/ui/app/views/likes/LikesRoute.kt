package com.dluvian.nozzle.ui.app.views.likes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun LikesRoute(
    likesViewModel: LikesViewModel,
    profileFollower: IProfileFollower,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val isRefreshing by likesViewModel.isRefreshing.collectAsState()
    val feedFlow by likesViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    LikesScreen(
        feed = feed,
        isRefreshing = isRefreshing,
        postCardNavLambdas = postCardNavLambdas,
        onShowMedia = { mediaUrl ->
            likesViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            likesViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefresh = likesViewModel.onRefresh,
        onLoadMore = likesViewModel.onLoadMore,
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
