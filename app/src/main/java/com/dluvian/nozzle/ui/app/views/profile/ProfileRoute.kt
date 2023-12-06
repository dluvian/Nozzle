package com.dluvian.nozzle.ui.app.views.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun ProfileRoute(
    profileViewModel: ProfileViewModel,
    profileFollower: IProfileFollower,
    postCardNavLambdas: PostCardNavLambdas,
    onOpenFollowerList: (String) -> Unit,
    onOpenFollowedByList: (String) -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    val isRefreshing by profileViewModel.isRefreshing.collectAsState()
    val profile by profileViewModel.profileState.collectAsState()
    val feedFlow by profileViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    ProfileScreen(
        isRefreshing = isRefreshing,
        profile = profile,
        isFollowedByMe = TODO(),
        feed = feed,
        postCardNavLambdas = postCardNavLambdas,
        onPrepareReply = onPrepareReply,
        onLike = { post ->
            profileViewModel.postCardInteractor.like(
                scope = profileViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onFollow = { pubkeyToFollow ->
            profileFollower.follow(pubkeyToFollow = pubkeyToFollow)
        },
        onUnfollow = { pubkeyToUnfollow ->
            profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
        },
        onOpenFollowerList = onOpenFollowerList,
        onOpenFollowedByList = onOpenFollowedByList,
        onShowMedia = { mediaUrl ->
            profileViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            profileViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefresh = profileViewModel.onRefresh,
        onLoadMore = profileViewModel.onLoadMore,
        onNavigateToEditProfile = onNavigateToEditProfile,
    )
}
