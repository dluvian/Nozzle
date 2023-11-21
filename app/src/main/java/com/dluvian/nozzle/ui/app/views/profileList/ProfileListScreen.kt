package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.components.FollowButton
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.hint.EmptyListHint
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardProfilePicture
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun ProfileListScreen(
    profileList: ProfileList,
    onFollow: (Int) -> Unit,
    onUnfollow: (Int) -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit,
    onGoBack: () -> Unit
) {
    val followerListStr = stringResource(id = R.string.following)
    val followedByStr = stringResource(id = R.string.followers)
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
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.medium, horizontal = spacing.screenEdge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PostCardProfilePicture(
                    modifier = Modifier.size(sizing.profilePicture),
                    pictureUrl = profile.picture,
                    pubkey = profile.pubkey,
                    trustType = TrustType.determineTrustType(
                        isOneself = profile.isOneself,
                        isFollowed = profile.isFollowedByMe,
                        trustScore = profile.trustScore,
                    ),
                    onNavigateToProfile = onNavigateToProfile
                )
                Spacer(Modifier.width(spacing.large))
                Text(text = profile.name.ifBlank {
                    ShortenedNameUtils.getShortenedNpubFromPubkey(profile.pubkey).orEmpty()
                })
            }
            FollowButton(
                isFollowed = profile.isFollowedByMe,
                onFollow = onFollow,
                onUnfollow = onUnfollow,
            )
        }
    }
}
