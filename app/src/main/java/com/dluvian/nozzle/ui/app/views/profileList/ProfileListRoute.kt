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
    val profileList by profileListViewModel.profileList.collectAsState()
    val forcedFollowState by profileListViewModel.forcedFollowState.collectAsState()

    val adjustedProfileList = remember(profileList, forcedFollowState) {
        if (forcedFollowState.isEmpty()) return@remember profileList
        profileList.let { list ->
            val profiles = list.profiles.map {
                val followState = forcedFollowState[it.pubkey] ?: return@map it
                it.copy(isFollowedByMe = followState)
            }
            list.copy(profiles = profiles)
        }
    }

    ProfileListScreen(
        profileList = adjustedProfileList,
        onFollow = profileListViewModel.onFollow,
        onUnfollow = profileListViewModel.onUnfollow,
        onNavigateToProfile = onNavigateToProfile,
        onGoBack = onGoBack
    )
}
