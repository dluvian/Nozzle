package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

@Composable
fun ProfileListRoute(
    profileListViewModel: ProfileListViewModel,
    showProfilePicture: Boolean,
    profileFollower: IProfileFollower,
    postCardLambdas: PostCardLambdas,
    onGoBack: () -> Unit,
) {
    val uiState by profileListViewModel.uiState.collectAsState()
    val profilesFlow by profileListViewModel.profiles.collectAsState()
    val profiles by profilesFlow.collectAsState()
    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedProfiles = remember(forceFollowed, profiles) {
        profiles.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    ProfileListScreen(
        uiState = uiState,
        profiles = adjustedProfiles,
        showProfilePicture = showProfilePicture,
        onFollow = postCardLambdas.onFollow,
        onUnfollow = postCardLambdas.onUnfollow,
        onLoadMore = profileListViewModel.onLoadMore,
        onSubscribeToUnknowns = profileListViewModel.onSubscribeToUnknowns,
        onNavigateToProfile = postCardLambdas.navLambdas.onNavigateToProfile,
        onGoBack = onGoBack
    )
}
