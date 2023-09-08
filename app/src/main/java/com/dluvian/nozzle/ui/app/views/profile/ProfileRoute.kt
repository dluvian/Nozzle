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
    onNavigateToThread: (String, String?) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    val isRefreshing by profileViewModel.isRefreshingState.collectAsState()
    val profile by profileViewModel.profileState.collectAsState()
    val feed by profileViewModel.feedState.collectAsState()

    ProfileScreen(
        isRefreshing = isRefreshing,
        profile = profile,
        feed = feed,
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
        onCopyNpub = profileViewModel.onCopyNpub,
        onLoadMore = profileViewModel.onLoadMore,
        onNavigateToThread = { postIds ->
            onNavigateToThread(
                postIds.id,
                postIds.replyToId,
            )
        },
        onNavigateToReply = onNavigateToReply,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onNavigateToQuote = onNavigateToQuote
    )
}
