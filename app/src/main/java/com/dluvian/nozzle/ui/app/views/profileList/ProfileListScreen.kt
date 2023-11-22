package com.dluvian.nozzle.ui.app.views.profileList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.components.FollowButton
import com.dluvian.nozzle.ui.components.ItemRow
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
    onSubscribeToUnknowns: (Pubkey) -> Unit,
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

    val subscribeToUnknowns = remember(profileList) { mutableStateOf(false) }
    LaunchedEffect(key1 = subscribeToUnknowns) {
        if (subscribeToUnknowns.value) onSubscribeToUnknowns(profileList.pubkey)
    }

    Column {
        ReturnableTopBar(text = title, onGoBack = onGoBack)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(profileList.profiles) { i, profile ->
                if (profile.name.isEmpty()) subscribeToUnknowns.value = true
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

@Composable
private fun PictureAndName(profile: SimpleProfile, onNavigateToProfile: (Pubkey) -> Unit) {
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
    Text(
        text = profile.name.ifBlank {
            ShortenedNameUtils.getShortenedNpubFromPubkey(profile.pubkey).orEmpty()
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,

        )
}
