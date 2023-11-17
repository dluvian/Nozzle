package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.components.FollowButton
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.hint.EmptyListHint
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardProfilePicture

@Composable
fun ProfileListScreen(
    profileList: ProfileList,
    onFollow: (Int) -> Unit,
    onUnfollow: (Int) -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit,
    onGoBack: () -> Unit
) {
    val followerListStr = stringResource(id = R.string.follower_list)
    val followedByStr = stringResource(id = R.string.followed_by)
    val title = remember(profileList.type) {
        when (profileList.type) {
            ProfileListType.FOLLOWER_LIST -> followerListStr
            ProfileListType.FOLLOWED_BY_LIST -> followedByStr
        }
    }

    Column {
        ReturnableTopBar(text = title, onGoBack = onGoBack)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(profileList.profiles) { i, profile ->
                ProfileRow(
                    profile = profile,
                    onFollow = { onFollow(i) },
                    onUnfollow = { onUnfollow(i) },
                    onNavigateToProfile,
                )
            }
        }
    }
    if (profileList.profiles.isEmpty()) EmptyListHint()
}

@Composable
private fun ProfileRow(
    profile: SimpleProfile,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit
) {
    Row {
        PostCardProfilePicture(
            pictureUrl = profile.picture,
            pubkey = profile.pubkey,
            trustType = TrustType.determineTrustType(
                pubkey = profile.pubkey,
                isOneself = profile.isOneself,
                isFollowed = profile.isFollowedByMe,
                trustScore = profile.trustScore,
            ),
            onNavigateToProfile = onNavigateToProfile
        )
        Text(text = profile.name)
        FollowButton(
            isFollowed = profile.isFollowedByMe,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
        )
    }
}
