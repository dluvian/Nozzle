package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.dluvian.nozzle.ui.components.FollowButton
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.TopBarCircleProgressIndicator
import com.dluvian.nozzle.ui.components.hint.EmptyListHint
import com.dluvian.nozzle.ui.components.itemRow.ItemRow
import com.dluvian.nozzle.ui.components.itemRow.PictureAndName

@Composable
fun ProfileListScreen(
    profiles: List<SimpleProfile>,
    pubkey: Pubkey,
    isRefreshing: Boolean,
    type: ProfileListType,
    onFollow: (Int) -> Unit,
    onUnfollow: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onSubscribeToUnknowns: (Pubkey) -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit,
    onGoBack: () -> Unit
) {
    val followerListStr = stringResource(id = R.string.following)
    val followedByStr = stringResource(id = R.string.followers)
    val title = remember(type) {
        when (type) {
            ProfileListType.FOLLOWER_LIST -> followerListStr
            ProfileListType.FOLLOWED_BY_LIST -> followedByStr
        }
    }

    val subscribeToUnknowns = remember(profiles.size) { mutableStateOf(false) }
    LaunchedEffect(key1 = subscribeToUnknowns) {
        if (subscribeToUnknowns.value) onSubscribeToUnknowns(pubkey)
    }

    Column {
        ReturnableTopBar(
            text = title,
            onGoBack = onGoBack,
            trailingIcon = { if (isRefreshing) TopBarCircleProgressIndicator() })
        val lastPubkey = remember { mutableStateOf("") }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(profiles) { i, profile ->
                if (profile.name.isEmpty()) subscribeToUnknowns.value = true
                ProfileRow(
                    profile = profile,
                    onFollow = { onFollow(i) },
                    onUnfollow = { onUnfollow(i) },
                    onNavigateToProfile,
                )
                if (i == profiles.size - 3
                    && profiles.size >= MAX_LIST_LENGTH
                ) {
                    lastPubkey.value = profiles.lastOrNull()?.pubkey.orEmpty()
                    onLoadMore()
                }
            }
        }
    }
    if (profiles.isEmpty()) EmptyListHint()
}

@Composable
private fun ProfileRow(
    profile: SimpleProfile,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit
) {
    ItemRow(
        content = { PictureAndName(profile = profile, onNavigateToProfile = onNavigateToProfile) },
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
