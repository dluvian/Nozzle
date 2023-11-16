package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.hint.EmptyListHint

@Composable
fun ProfileListScreen(
    profileList: ProfileList,
    isRefreshing: Boolean,
    onFollow: (Int) -> Unit,
    onUnfollow: (Int) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit,
    onGoBack: () -> Unit
) {
    val followerListStr = stringResource(id = R.string.follower_list)
    val followedByStr = stringResource(id = R.string.followed_by)
    val title = remember(profileList) {
        when (profileList) {
            is FollowerList -> followerListStr
            is FollowedByList -> followedByStr
        }
    }

    Column {
        ReturnableTopBar(text = title, onGoBack = onGoBack)
        Column(modifier = Modifier.fillMaxSize()) {
            profileList.profiles.forEach {
                Text(text = it.name)
            }
        }
    }
    if (profileList.profiles.isEmpty()) EmptyListHint()
}
