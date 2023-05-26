package com.dluvian.nozzle.ui.app.views.profile

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dluvian.nozzle.model.PostWithMeta

private const val TAG = "ProfileRoute"

@Composable
fun ProfileRoute(
    profileViewModel: ProfileViewModel,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToThread: (String, String?, String?) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    val isRefreshing by profileViewModel.isRefreshingState.collectAsState()
    val profile by profileViewModel.profileState.collectAsState()
    val feed by profileViewModel.feedState.collectAsState()
    val forceRecomposition by profileViewModel.forceRecompositionState.collectAsState()
    Log.d(TAG, "Recompose $forceRecomposition")

    ProfileScreen(
        isRefreshing = isRefreshing,
        profile = profile,
        feed = feed,
        onPrepareReply = onPrepareReply,
        onLike = profileViewModel.onLike,
        onRepost = profileViewModel.onRepost,
        onFollow = profileViewModel.onFollow,
        onUnfollow = profileViewModel.onUnfollow,
        onRefreshProfileView = profileViewModel.onRefreshProfileView,
        onCopyNpub = profileViewModel.onCopyNpub,
        onLoadMore = profileViewModel.onLoadMore,
        onNavigateToThread = { postIds ->
            onNavigateToThread(
                postIds.id,
                postIds.replyToId,
                postIds.replyToRootId
            )
        },
        onNavigateToReply = onNavigateToReply,
        onNavigateToEditProfile = onNavigateToEditProfile,
    )
}
