package com.dluvian.nozzle.ui.app.views.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.PostWithMeta

@Composable
fun ProfileRoute(
    profileViewModel: ProfileViewModel,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToThread: (String) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToId: (String) -> Unit
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
        onNavigateToThread = onNavigateToThread,
        onNavigateToReply = onNavigateToReply,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onNavigateToQuote = onNavigateToQuote,
        onNavigateToId = onNavigateToId,
    )
}
