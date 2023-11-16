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
    val profileList by profileListViewModel.profileList.collectAsState()
    val forcedFollowState by profileListViewModel.forcedFollowState.collectAsState()

    val adjustedProfileList = remember(profileList, forcedFollowState) {
        profileList?.let { list ->
            val profiles = list.profiles.map {
                val followState = forcedFollowState[it.pubkey] ?: return@map it
                it.copy(isFollowedByMe = followState)
            }
            when (list) {
                is FollowerList -> FollowerList(profiles = profiles)
                is FollowedByList -> FollowedByList(profiles = profiles)
            }
        }
    }

    if (adjustedProfileList != null) {
        ProfileListScreen(
            profileList = adjustedProfileList,
            isRefreshing = isRefreshing,
            onFollow = profileListViewModel.onFollow,
            onUnfollow = profileListViewModel.onUnfollow,
            onRefresh = profileListViewModel.onRefresh,
            onNavigateToProfile = onNavigateToProfile,
            onGoBack = onGoBack
        )
    }
}
