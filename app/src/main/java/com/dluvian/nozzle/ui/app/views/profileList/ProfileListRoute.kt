package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dluvian.nozzle.model.Pubkey

@Composable
fun ProfileListRoute(
    profileListViewModel: ProfileListViewModel,
    onNavigateToProfile: (Pubkey) -> Unit,
    onGoBack: () -> Unit,
) {
    val isRefreshing by profileListViewModel.isRefreshing.collectAsState()
    val forcedFollowState by profileListViewModel.forcedFollowState.collectAsState()
    val profilesFlow by profileListViewModel.profiles.collectAsState()
    val profiles by profilesFlow.collectAsState()
    val type by profileListViewModel.type
    val pubkey by profileListViewModel.pubkey

    val adjustedProfiles = remember(profiles, forcedFollowState) {
        if (forcedFollowState.isEmpty()) return@remember profiles
        profiles.let { list ->
            list.map {
                val followState = forcedFollowState[it.pubkey] ?: return@map it
                it.copy(isFollowedByMe = followState)
            }
        }
    }

    ProfileListScreen(
        profiles = adjustedProfiles,
        pubkey = pubkey,
        isRefreshing = isRefreshing,
        type = type,
        onFollow = profileListViewModel.onFollow,
        onUnfollow = profileListViewModel.onUnfollow,
        onLoadMore = profileListViewModel.onLoadMore,
        onSubscribeToUnknowns = profileListViewModel.onSubscribeToUnknowns,
        onNavigateToProfile = onNavigateToProfile,
        onGoBack = onGoBack
    )
}
