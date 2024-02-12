package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.MAX_LIST_LENGTH
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.ui.components.buttons.FollowButton
import com.dluvian.nozzle.ui.components.hint.EmptyListHint
import com.dluvian.nozzle.ui.components.indicators.TopBarCircleProgressIndicator
import com.dluvian.nozzle.ui.components.rows.ItemRow
import com.dluvian.nozzle.ui.components.rows.PictureAndName
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun ProfileListScreen(
    uiState: ProfileListViewModelState,
    profiles: List<SimpleProfile>,
    showProfilePicture: Boolean,
    onFollow: (Pubkey) -> Unit,
    onUnfollow: (Pubkey) -> Unit,
    onLoadMore: () -> Unit,
    onSubscribeToUnknowns: (Pubkey) -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit,
    onGoBack: () -> Unit
) {

    ReturnableScaffold(
        topBarText = getTitle(type = uiState.type),
        onGoBack = onGoBack,
        actions = {
            if (uiState.isRefreshing) {
                TopBarCircleProgressIndicator()
                Spacer(modifier = Modifier.width(spacing.screenEdge))
            }
        }
    ) {
        val subscribeToUnknowns = remember(profiles.size) { mutableStateOf(false) }
        LaunchedEffect(key1 = subscribeToUnknowns) {
            if (subscribeToUnknowns.value) onSubscribeToUnknowns(uiState.pubkey)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(profiles) { i, profile ->
                if (profile.name.isEmpty()) subscribeToUnknowns.value = true
                ProfileRow(
                    profile = profile,
                    showProfilePicture = showProfilePicture,
                    onFollow = { onFollow(profile.pubkey) },
                    onUnfollow = { onUnfollow(profile.pubkey) },
                    onNavigateToProfile,
                )
                if (i == profiles.size - 3 && profiles.size >= MAX_LIST_LENGTH) onLoadMore()
            }
        }
        if (profiles.isEmpty()) EmptyListHint()
    }
}

@Composable
private fun getTitle(type: ProfileListType): String {
    val followerListStr = stringResource(id = R.string.following)
    val followedByStr = stringResource(id = R.string.followers)
    return remember(type) {
        when (type) {
            ProfileListType.FOLLOWER_LIST -> followerListStr
            ProfileListType.FOLLOWED_BY_LIST -> followedByStr
        }
    }
}

@Composable
private fun ProfileRow(
    profile: SimpleProfile,
    showProfilePicture: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit
) {
    ItemRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.medium, horizontal = spacing.screenEdge),
        content = {
            PictureAndName(
                profile = profile,
                showProfilePicture = showProfilePicture,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        onClick = { onNavigateToProfile(profile.pubkey) },
        trailingContent = {
            FollowButton(
                isFollowed = profile.isFollowedByMe,
                onFollow = onFollow,
                onUnfollow = onUnfollow,
            )
        }
    )
}
