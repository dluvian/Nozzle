package com.dluvian.nozzle.ui.app.views.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.media3.exoplayer.ExoPlayer
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

@Composable
fun ProfileRoute(
    profileViewModel: ProfileViewModel,
    videoPlayer: ExoPlayer,
    profileFollower: IProfileFollower,
    postCardLambdas: PostCardLambdas,
    onOpenFollowerList: (String) -> Unit,
    onOpenFollowedByList: (String) -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    val isRefreshing by profileViewModel.isRefreshing.collectAsState()
    val profile by profileViewModel.profileState.collectAsState()

    val feedFlow by profileViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    val numOfNewPostsFlow by profileViewModel.numOfNewPosts.collectAsState()
    val numOfNewPosts by numOfNewPostsFlow.collectAsState()

    val contactList by profileViewModel.contactList.collectAsState()
    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedFeed = remember(forceFollowed, feed) {
        feed.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    ProfileScreen(
        isRefreshing = isRefreshing,
        profile = profile,
        isFollowedByMe = forceFollowed[profile.pubkey] ?: contactList.contains(profile.pubkey),
        feed = adjustedFeed,
        numOfNewPosts = numOfNewPosts,
        videoPlayer = videoPlayer,
        postCardLambdas = postCardLambdas,
        onPrepareReply = onPrepareReply,
        onOpenFollowerList = onOpenFollowerList,
        onOpenFollowedByList = onOpenFollowedByList,
        onRefresh = profileViewModel.onRefresh,
        onLoadMore = profileViewModel.onLoadMore,
        onNavigateToEditProfile = onNavigateToEditProfile,
    )
}
