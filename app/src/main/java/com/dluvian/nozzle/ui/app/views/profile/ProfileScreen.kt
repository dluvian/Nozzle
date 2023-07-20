package com.dluvian.nozzle.ui.app.views.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.model.PostIds
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ProfileWithAdditionalInfo
import com.dluvian.nozzle.model.determineTrustType
import com.dluvian.nozzle.ui.components.CopyIcon
import com.dluvian.nozzle.ui.components.EditProfileButton
import com.dluvian.nozzle.ui.components.FollowButton
import com.dluvian.nozzle.ui.components.ProfilePicture
import com.dluvian.nozzle.ui.components.dialog.RelaysDialog
import com.dluvian.nozzle.ui.components.postCard.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList
import com.dluvian.nozzle.ui.components.text.HyperlinkedText
import com.dluvian.nozzle.ui.components.text.NumberedCategory
import com.dluvian.nozzle.ui.theme.LightGray21
import com.dluvian.nozzle.ui.theme.Shapes
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun ProfileScreen(
    isRefreshing: Boolean,
    profile: ProfileWithAdditionalInfo,
    feed: List<PostWithMeta>,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLike: (String) -> Unit,
    onRepost: (String) -> Unit,
    onFollow: (String) -> Unit,
    onUnfollow: (String) -> Unit,
    onRefreshProfileView: () -> Unit,
    onCopyNpub: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToThread: (PostIds) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    Column {
        ProfileData(
            profile = profile,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onCopyNpub = onCopyNpub,
            onNavToEditProfile = onNavigateToEditProfile,
        )
        Spacer(Modifier.height(spacing.medium))
        NumberedCategories(
            numOfFollowing = profile.numOfFollowing,
            numOfFollowers = profile.numOfFollowers,
            relays = profile.relays,
        )
        Spacer(Modifier.height(spacing.xl))
        Divider()
        PostCardList(
            posts = feed,
            isRefreshing = isRefreshing,
            onRefresh = onRefreshProfileView,
            onLike = onLike,
            onRepost = onRepost,
            onPrepareReply = onPrepareReply,
            onLoadMore = onLoadMore,
            onNavigateToThread = onNavigateToThread,
            onNavigateToReply = onNavigateToReply
        )
    }
    if (feed.isEmpty()) {
        NoPostsHint()
    }
}

@Composable
private fun ProfileData(
    profile: ProfileWithAdditionalInfo,
    onFollow: (String) -> Unit,
    onUnfollow: (String) -> Unit,
    onCopyNpub: () -> Unit,
    onNavToEditProfile: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = spacing.screenEdge),
        verticalArrangement = Arrangement.Center
    ) {
        ProfilePictureAndActions(
            pictureUrl = profile.metadata.picture.orEmpty(),
            pubkey = profile.pubkey,
            isOneself = profile.isOneself,
            isFollowed = profile.isFollowedByMe,
            followedByFriendsPercentage = profile.followedByFriendsPercentage,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onNavToEditProfile = onNavToEditProfile,
        )
        NameAndNpub(
            name = profile.metadata.name.orEmpty()
                .ifEmpty { getShortenedNpubFromPubkey(profile.pubkey) },
            npub = profile.npub,
            onCopyNpub = onCopyNpub,
        )
        Spacer(Modifier.height(spacing.medium))
        profile.metadata.about?.let { about ->
            if (about.isNotBlank()) {
                HyperlinkedText(
                    text = about.trim(),
                    maxLines = 3,
                    onClickNonLink = { /*Do nothing*/ })
            }
        }
    }
}

@Composable
private fun ProfilePictureAndActions(
    pictureUrl: String,
    pubkey: String,
    isOneself: Boolean,
    isFollowed: Boolean,
    followedByFriendsPercentage: Float?,
    onFollow: (String) -> Unit,
    onUnfollow: (String) -> Unit,
    onNavToEditProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(spacing.screenEdge)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProfilePicture(
            modifier = Modifier
                .size(sizing.largeProfilePicture)
                .aspectRatio(1f),
            pictureUrl = pictureUrl,
            pubkey = pubkey,
            trustType = determineTrustType(
                isOneself = isOneself,
                isFollowed = isFollowed,
                followedByFriendsPercentage = followedByFriendsPercentage,
            )
        )
        FollowOrEditButton(
            isOneself = isOneself,
            isFollowed = isFollowed,
            onFollow = { onFollow(pubkey) },
            onUnfollow = { onUnfollow(pubkey) },
            onNavToEditProfile = onNavToEditProfile,
        )
    }
}

@Composable
private fun FollowOrEditButton(
    isOneself: Boolean,
    isFollowed: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onNavToEditProfile: () -> Unit,
) {
    if (isOneself) {
        EditProfileButton(onNavToEditProfile = onNavToEditProfile)
    } else {
        FollowButton(
            isFollowed = isFollowed,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
        )
    }
}

@Composable
private fun NumberedCategories(
    numOfFollowing: Int,
    numOfFollowers: Int,
    relays: List<String>,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xl),
    ) {
        Row {
            NumberedCategory(
                number = numOfFollowing,
                category = stringResource(id = R.string.following)
            )
            Spacer(Modifier.width(spacing.large))
            NumberedCategory(
                number = numOfFollowers,
                category = stringResource(id = R.string.followers)
            )
            Spacer(Modifier.width(spacing.large))
            val openRelayDialog = remember { mutableStateOf(false) }
            if (openRelayDialog.value) {
                RelaysDialog(relays = relays, onCloseDialog = { openRelayDialog.value = false })
            }
            NumberedCategory(
                modifier = Modifier
                    .clip(Shapes.small)
                    .clickable { openRelayDialog.value = true },
                number = relays.size,
                category = stringResource(id = R.string.relays)
            )
        }
    }
}

@Composable
private fun NameAndNpub(
    name: String,
    npub: String,
    onCopyNpub: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.padding(end = spacing.medium)) {
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.h6,
            )
            CopyableNpub(
                npub = npub,
                onCopyNpub = onCopyNpub
            )
        }
    }
}

@Composable
private fun CopyableNpub(
    npub: String,
    onCopyNpub: () -> Unit,
) {
    Row(
        Modifier.clickable { onCopyNpub() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        CopyIcon(
            modifier = Modifier.size(sizing.smallIcon),
            description = stringResource(id = R.string.copy_pubkey),
            tint = LightGray21
        )
        Text(
            text = npub,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = LightGray21,
            style = MaterialTheme.typography.body2,
        )
    }
}
