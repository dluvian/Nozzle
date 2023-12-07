package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.Pubkey

@Composable
fun ProfileListRoute(
    profileListViewModel: ProfileListViewModel,
    profileFollower: IProfileFollower,
    onNavigateToProfile: (Pubkey) -> Unit,
    onGoBack: () -> Unit,
) {
    val isRefreshing by profileListViewModel.isRefreshing.collectAsState()
    val profilesFlow by profileListViewModel.profiles.collectAsState()
    val profiles by profilesFlow.collectAsState()
    val forceFollowed by profileFollower.getForceFollowedState()
    val type by profileListViewModel.type
    val pubkey by profileListViewModel.pubkey
    val adjustedProfiles = remember(forceFollowed, profiles) {
        profiles.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    ProfileListScreen(
        profiles = adjustedProfiles,
        pubkey = pubkey,
        isRefreshing = isRefreshing,
        type = type,
        onFollow = { pubkeyToFollow ->
            profileFollower.follow(pubkeyToFollow = pubkeyToFollow)
        },
        onUnfollow = { pubkeyToUnfollow ->
            profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
        },
        onLoadMore = profileListViewModel.onLoadMore,
        onSubscribeToUnknowns = profileListViewModel.onSubscribeToUnknowns,
        onNavigateToProfile = onNavigateToProfile,
        onGoBack = onGoBack
    )
}
