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
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    val isRefreshing by profileViewModel.isRefreshingState.collectAsState()
    val profile by profileViewModel.profileState.collectAsState()
    val feed by profileViewModel.feedState.collectAsState()
    val isFollowedByMe by profileViewModel.isFollowedByMeState.collectAsState()

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
        onShowMedia = { mediaUrl ->
            profileViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            profileViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefreshProfileView = profileViewModel.onRefreshProfileView,
        onCopyNprofile = profileViewModel.onCopyNprofile,
        onLoadMore = profileViewModel.onLoadMore,
        onNavigateToEditProfile = onNavigateToEditProfile,
    )
}
