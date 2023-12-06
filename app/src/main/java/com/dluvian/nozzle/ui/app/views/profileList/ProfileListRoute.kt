package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
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
    val type by profileListViewModel.type
    val pubkey by profileListViewModel.pubkey

    ProfileListScreen(
        profiles = profiles,
        pubkey = pubkey,
        isRefreshing = isRefreshing,
        type = type,
        onFollow = { pubkeyToFollow: Pubkey ->
            profileFollower.follow(
                scope = profileListViewModel.viewModelScope,
                pubkeyToFollow = pubkeyToFollow
            )
        },
        onUnfollow = { pubkeyToUnfollow: Pubkey ->
            profileFollower.unfollow(
                scope = profileListViewModel.viewModelScope,
                pubkeyToUnfollow = pubkeyToUnfollow
            )
        },
        onLoadMore = profileListViewModel.onLoadMore,
        onSubscribeToUnknowns = profileListViewModel.onSubscribeToUnknowns,
        onNavigateToProfile = onNavigateToProfile,
        onGoBack = onGoBack
    )
}
