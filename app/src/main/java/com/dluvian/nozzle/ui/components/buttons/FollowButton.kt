package com.dluvian.nozzle.ui.components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R


@Composable
fun FollowButton(
    isFollowed: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit
) {
    HollowButton(
        text = if (isFollowed) stringResource(id = R.string.following) else stringResource(id = R.string.follow),
        isActive = isFollowed,
        onClick = if (isFollowed) onUnfollow else onFollow
    )
}
