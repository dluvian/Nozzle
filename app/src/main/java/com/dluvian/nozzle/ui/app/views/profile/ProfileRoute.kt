package com.dluvian.nozzle.ui.app.views.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun ProfileRoute(
    profileViewModel: ProfileViewModel,
    postCardNavLambdas: PostCardNavLambdas,
    onOpenFollowerList: (String) -> Unit,
    onOpenFollowedByList: (String) -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    val isRefreshing by profileViewModel.isRefreshing.collectAsState()
    val profile by profileViewModel.profileState.collectAsState()
    val isFollowedByMe by profileViewModel.isFollowedByMeState.collectAsState()
    val feedFlow by profileViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    ProfileScreen(
        isRefreshing = isRefreshing,
        profile = profile,
        isFollowedByMe = isFollowedByMe,
        feed = feed.map { it.copy(isFollowedByMe = isFollowedByMe) },
        postCardNavLambdas = postCardNavLambdas,
        onPrepareReply = onPrepareReply,
        onLike = { post ->
            profileViewModel.postCardInteractor.like(
                scope = profileViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onFollow = profileViewModel.onFollow,
        onUnfollow = profileViewModel.onUnfollow,
        onOpenFollowerList = onOpenFollowerList,
        onOpenFollowedByList = onOpenFollowedByList,
        onShowMedia = { mediaUrl ->
            profileViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            profileViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefresh = profileViewModel.onRefresh,
        onCopyNprofile = profileViewModel.onCopyNprofile,
        onLoadMore = profileViewModel.onLoadMore,
        onNavigateToEditProfile = onNavigateToEditProfile,
    )
}
