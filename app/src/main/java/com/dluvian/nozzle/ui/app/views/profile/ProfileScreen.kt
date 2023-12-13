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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.utils.copyAndToast
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ProfileWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas
import com.dluvian.nozzle.ui.components.CopyIcon
import com.dluvian.nozzle.ui.components.EditProfileButton
import com.dluvian.nozzle.ui.components.FollowButton
import com.dluvian.nozzle.ui.components.LightningIcon
import com.dluvian.nozzle.ui.components.dialog.RelaysDialog
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.components.postCard.PostCardList
import com.dluvian.nozzle.ui.components.text.AnnotatedText
import com.dluvian.nozzle.ui.components.text.NumberedCategory
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun ProfileScreen(
    isRefreshing: Boolean,
    profile: ProfileWithMeta,
    isFollowedByMe: Boolean,
    feed: List<PostWithMeta>,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLike: (PostWithMeta) -> Unit,
    onFollow: (Pubkey) -> Unit,
    onUnfollow: (Pubkey) -> Unit,
    onOpenFollowerList: (String) -> Unit,
    onOpenFollowedByList: (String) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    Column {
        ProfileData(
            profile = profile,
            isFollowedByMe = isFollowedByMe,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onNavToEditProfile = onNavigateToEditProfile,
            onNavigateToId = postCardNavLambdas.onNavigateToId,
        )
        Spacer(Modifier.height(spacing.medium))
        NumberedCategories(
            numOfFollowing = profile.numOfFollowing,
            numOfFollowers = profile.numOfFollowers,
            relays = profile.relays,
            onOpenFollowerList = { onOpenFollowerList(profile.pubkey) },
            onOpenFollowedByList = { onOpenFollowedByList(profile.pubkey) }
        )
        Spacer(Modifier.height(spacing.xl))
        Divider()
        PostCardList(
            posts = feed,
            isRefreshing = isRefreshing,
            postCardNavLambdas = postCardNavLambdas,
            onRefresh = onRefresh,
            onLike = onLike,
            onShowMedia = onShowMedia,
            onShouldShowMedia = onShouldShowMedia,
            onPrepareReply = onPrepareReply,
            onLoadMore = onLoadMore,
            onFollow = onFollow,
            onUnfollow = onUnfollow
        )
    }
    if (feed.isEmpty()) NoPostsHint()
}

@Composable
private fun ProfileData(
    profile: ProfileWithMeta,
    isFollowedByMe: Boolean,
    onFollow: (Pubkey) -> Unit,
    onUnfollow: (Pubkey) -> Unit,
    onNavToEditProfile: () -> Unit,
    onNavigateToId: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = spacing.screenEdge),
        verticalArrangement = Arrangement.Center
    ) {
        ProfilePictureAndActions(
            pubkey = profile.pubkey,
            isOneself = profile.isOneself,
            isFollowed = isFollowedByMe,
            trustScore = profile.trustScore,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onNavToEditProfile = onNavToEditProfile,
        )
        ProfileStrings(
            name = profile.metadata.name.orEmpty()
                .ifEmpty { getShortenedNpubFromPubkey(profile.pubkey) ?: profile.pubkey },
            nprofile = profile.nprofile,
            lud16 = profile.metadata.lud16
        )
        Spacer(Modifier.height(spacing.medium))
        profile.metadata.about?.let { about ->
            if (about.isNotBlank()) {
                AnnotatedText(
                    text = AnnotatedString(about), // TODO: Annotate about
                    maxLines = 3,
                    onNavigateToId = onNavigateToId,
                    onClickNonLink = { /*Do nothing*/ })
            }
        }
    }
}

@Composable
private fun ProfilePictureAndActions(
    pubkey: String,
    isOneself: Boolean,
    isFollowed: Boolean,
    trustScore: Float?,
    onFollow: (String) -> Unit,
    onUnfollow: (String) -> Unit,
    onNavToEditProfile: () -> Unit,
) {
    val trustType = remember(isOneself, isFollowed, trustScore) {
        TrustType.determineTrustType(
            isOneself = isOneself,
            isFollowed = isFollowed,
            trustScore = trustScore,
        )
    }
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
            pubkey = pubkey,
            trustType = trustType
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
    onOpenFollowerList: () -> Unit,
    onOpenFollowedByList: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xl),
    ) {
        Row {
            NumberedCategory(
                number = numOfFollowing,
                category = stringResource(id = R.string.following),
                onClick = onOpenFollowerList
            )
            Spacer(Modifier.width(spacing.large))
            NumberedCategory(
                number = numOfFollowers,
                category = stringResource(id = R.string.followers),
                onClick = onOpenFollowedByList
            )
            Spacer(Modifier.width(spacing.large))
            val openRelayDialog = remember { mutableStateOf(false) }
            if (openRelayDialog.value) {
                RelaysDialog(relays = relays, onCloseDialog = { openRelayDialog.value = false })
            }
            NumberedCategory(
                number = relays.size,
                category = stringResource(id = R.string.relays),
                onClick = { openRelayDialog.value = true }
            )
        }
    }
}

@Composable
private fun ProfileStrings(
    name: String,
    nprofile: String,
    lud16: String?
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
            val clip = LocalClipboardManager.current
            val context = LocalContext.current
            CopyableNprofile(
                nprofile = nprofile,
                onCopyNprofile = {
                    copyAndToast(
                        text = nprofile,
                        toast = context.getString(R.string.profile_id_copied),
                        context = context,
                        clip = clip
                    )
                }
            )
            if (lud16 != null) {
                Spacer(modifier = Modifier.height(spacing.small))
                Lud16(
                    lud16 = lud16,
                    onCopyLud16 = {
                        copyAndToast(
                            text = lud16,
                            toast = context.getString(R.string.lightning_address_copied),
                            context = context,
                            clip = clip
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CopyableNprofile(nprofile: String, onCopyNprofile: () -> Unit) {
    ProfileStringRow(text = nprofile,
        onClick = onCopyNprofile,
        leadingIcon = {
            CopyIcon(modifier = Modifier.size(sizing.smallItem), tint = Color.LightGray)
        }
    )
}

@Composable
private fun Lud16(lud16: String, onCopyLud16: () -> Unit) {
    ProfileStringRow(
        text = lud16,
        onClick = onCopyLud16,
        leadingIcon = {
            LightningIcon(modifier = Modifier.size(sizing.smallItem), tint = Color.LightGray)
        },
    )
}

@Composable
private fun ProfileStringRow(
    text: String,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon()
        Spacer(modifier = Modifier.width(spacing.small))
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.LightGray,
            style = MaterialTheme.typography.body2,
        )
    }
}
