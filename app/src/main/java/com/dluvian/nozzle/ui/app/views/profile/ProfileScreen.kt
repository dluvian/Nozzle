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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.utils.copyAndToast
import com.dluvian.nozzle.data.utils.isScrollingUp
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ProfileWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.CopyIcon
import com.dluvian.nozzle.ui.components.EditProfileButton
import com.dluvian.nozzle.ui.components.FollowButton
import com.dluvian.nozzle.ui.components.LightningIcon
import com.dluvian.nozzle.ui.components.ShowNewPostsButton
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
    numOfNewPosts: Int,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onOpenFollowerList: (String) -> Unit,
    onOpenFollowedByList: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    ShowNewPostsButton(
        isVisible = !isRefreshing && numOfNewPosts > 0
                && (feed.size < DB_BATCH_SIZE || lazyListState.isScrollingUp()),
        numOfNewPosts = numOfNewPosts,
        lazyListState = lazyListState,
        onRefresh = onRefresh
    )
    Column {
        ProfileData(
            profile = profile,
            isFollowedByMe = isFollowedByMe,
            onFollow = postCardLambdas.onFollow,
            onUnfollow = postCardLambdas.onUnfollow,
            onNavToEditProfile = onNavigateToEditProfile,
            onNavigateToId = postCardLambdas.navLambdas.onNavigateToId,
        )
        Spacer(Modifier.height(spacing.medium))
        NumberedCategories(
            numOfFollowing = profile.numOfFollowing,
            numOfFollowers = profile.numOfFollowers,
            seenInRelays = profile.seenInRelays,
            writesInRelays = profile.writesInRelays,
            readsInRelays = profile.readsInRelays,
            onOpenFollowerList = { onOpenFollowerList(profile.pubkey) },
            onOpenFollowedByList = { onOpenFollowedByList(profile.pubkey) }
        )
        Spacer(Modifier.height(spacing.xl))
        Divider()
        PostCardList(
            posts = feed,
            isRefreshing = isRefreshing,
            postCardLambdas = postCardLambdas,
            onRefresh = onRefresh,
            onPrepareReply = onPrepareReply,
            onLoadMore = onLoadMore,
            lazyListState = lazyListState
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
    seenInRelays: List<String>,
    writesInRelays: List<String>,
    readsInRelays: List<String>,
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
                RelaysDialog(
                    seenInRelays = seenInRelays,
                    writesInRelays = writesInRelays,
                    readsInRelays = readsInRelays,
                    onCloseDialog = { openRelayDialog.value = false }
                )
            }
            NumberedCategory(
                number = seenInRelays.size,
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
                        toast = context.getString(R.string.copied_profile_id),
                        context = context,
                        clip = clip
                    )
                }
            )
            if (!lud16.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(spacing.small))
                Lud16(
                    lud16 = lud16,
                    onCopyLud16 = {
                        copyAndToast(
                            text = lud16,
                            toast = context.getString(R.string.copied_lightning_address),
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
