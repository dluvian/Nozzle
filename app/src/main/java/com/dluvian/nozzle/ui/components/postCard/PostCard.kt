package com.dluvian.nozzle.ui.components.postCard

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.utils.hexToNote
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.PostIds
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ThreadPosition
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.components.*
import com.dluvian.nozzle.ui.components.text.*
import com.dluvian.nozzle.ui.theme.*


// TODO: Split file into multiple

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: PostWithMeta,
    onLike: (String) -> Unit,
    onQuote: (String) -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToThread: (PostIds) -> Unit,
    onNavigateToReply: () -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    isCurrent: Boolean = false,
    threadPosition: ThreadPosition = ThreadPosition.SINGLE,
    onOpenProfile: ((String) -> Unit)? = null,
) {
    val x = sizing.profilePicture / 2 + spacing.screenEdge
    val yTop = spacing.screenEdge
    val yBottom = sizing.profilePicture + spacing.screenEdge
    val small = spacing.small
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Row(modifier
        .combinedClickable(enabled = !isCurrent,
            onClick = { onNavigateToThread(post.getPostIds()) },
            onLongClick = {
                clipboard.setText(AnnotatedString(hexToNote(post.id)))
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.note_id_copied),
                        Toast.LENGTH_SHORT
                    )
                    .show()

            })
        .fillMaxWidth()
        .drawBehind {
            when (threadPosition) {
                ThreadPosition.START -> {
                    drawThread(
                        scope = this,
                        x = x.toPx(),
                        yStart = yBottom.toPx(),
                        yEnd = size.height,
                        width = small.toPx()
                    )
                }

                ThreadPosition.MIDDLE -> {
                    drawThread(
                        scope = this,
                        x = x.toPx(),
                        yStart = 0f,
                        yEnd = yTop.toPx(),
                        width = small.toPx()
                    )
                    drawThread(
                        scope = this,
                        x = x.toPx(),
                        yStart = yBottom.toPx(),
                        yEnd = size.height,
                        width = small.toPx()
                    )
                }

                ThreadPosition.END -> {
                    drawThread(
                        scope = this,
                        x = x.toPx(),
                        yStart = 0f,
                        yEnd = yTop.toPx(),
                        width = small.toPx()
                    )
                }

                ThreadPosition.SINGLE -> {}
            }
        }
        .padding(all = spacing.screenEdge)
        .padding(end = spacing.medium)
        .clipToBounds()
    ) {
        PostCardProfilePicture(
            modifier = Modifier.size(sizing.profilePicture),
            pictureUrl = post.pictureUrl,
            pubkey = post.pubkey,
            trustType = TrustType.determineTrustType(
                pubkey = post.pubkey,
                isOneself = post.isOneself,
                isFollowed = post.isFollowedByMe,
                trustScore = post.trustScore,
            ),
            onOpenProfile = onOpenProfile,
        )
        Spacer(Modifier.width(spacing.large))
        Column {
            PostCardHeaderAndContent(
                post = post,
                isCurrent = isCurrent,
                onOpenProfile = onOpenProfile,
                onNavigateToThread = {
                    if (!isCurrent) {
                        onNavigateToThread(post.getPostIds())
                    }
                }
            )

            post.mediaUrl?.let { mediaUrl ->
                Spacer(Modifier.height(spacing.medium))
                MediaDecisionCard(
                    mediaUrl = mediaUrl,
                    onShowMedia = onShowMedia,
                    onShouldShowMedia = onShouldShowMedia,
                )
            }

            post.mentionedPost?.let { mentionedPost ->
                Spacer(Modifier.height(spacing.medium))
                MentionedCardContent(
                    post = mentionedPost,
                    onOpenProfile = onOpenProfile,
                    onNavigateToThread = onNavigateToThread,
                )
            }

            Spacer(Modifier.height(spacing.medium))
            PostCardActions(
                numOfReplies = post.numOfReplies,
                post = post,
                onLike = { onLike(post.id) },
                onQuote = { onQuote(post.id) },
                onPrepareReply = onPrepareReply,
                onNavigateToReply = onNavigateToReply,
            )
        }
    }
}

@Composable
private fun PostCardHeaderAndContent(
    post: PostWithMeta,
    isCurrent: Boolean,
    onOpenProfile: ((String) -> Unit)?,
    onNavigateToThread: () -> Unit,
) {
    Column {
        PostCardHeader(
            name = post.name.ifEmpty { getShortenedNpubFromPubkey(post.pubkey) },
            pubkey = post.pubkey,
            createdAt = post.createdAt,
            onOpenProfile = onOpenProfile
        )
        PostCardContentBase(
            replyToName = if (post.replyToId != null) {
                post.replyToName.orEmpty()
                    .ifEmpty {
                        post.replyToPubkey?.let { getShortenedNpubFromPubkey(post.replyToPubkey) }
                            .orEmpty()
                    }
                    .ifEmpty { "???" }
            } else null,
            replyRelayHint = post.replyRelayHint,
            relays = post.relays,
            content = post.content,
            isCurrent = isCurrent,
            onNavigateToThread = onNavigateToThread,
        )
    }
}

@Composable
private fun BorderedCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.border(
            width = spacing.tiny,
            color = Color.LightGray,
            shape = RoundedCornerShape(spacing.large)
        )
    ) {
        content()
    }
}

@Composable
private fun MentionedCardContent(
    post: MentionedPost,
    onOpenProfile: ((String) -> Unit)?,
    onNavigateToThread: (PostIds) -> Unit,
) {
    BorderedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToThread(post.toPostIds()) })
    {
        Column(modifier = Modifier.padding(spacing.large)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PostCardProfilePicture(
                    modifier = Modifier
                        .size(sizing.smallProfilePicture)
                        .clip(CircleShape),
                    pictureUrl = post.picture,
                    pubkey = post.pubkey,
                    trustType = Oneself, // TODO: Find correct trust type
                    onOpenProfile = onOpenProfile,
                )
                Spacer(modifier = Modifier.width(spacing.medium))
                PostCardHeader(
                    name = post.name,
                    pubkey = post.pubkey,
                    createdAt = post.createdAt,
                    onOpenProfile = onOpenProfile
                )
            }
            PostCardContentBase(
                replyToName = null,
                replyRelayHint = null,
                relays = null,
                content = post.content,
                isCurrent = false,
                onNavigateToThread = { onNavigateToThread(post.toPostIds()) }
            )
        }
    }
}

@Composable
private fun MediaDecisionCard(
    mediaUrl: String,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean
) {
    // showMedia is not in PostWithMeta because the posts before infinite scroll activates are cold
    // to save resources
    val showMedia = remember(mediaUrl) { mutableStateOf(onShouldShowMedia(mediaUrl)) }
    if (!showMedia.value) {
        ShowMediaCard(onClick = {
            onShowMedia(mediaUrl)
            showMedia.value = true
        })
    } else {
        LoadedMedia(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = spacing.tiny, color = Color.LightGray),
            mediaUrl = mediaUrl
        )
    }
}

@Composable
private fun LoadedMedia(mediaUrl: String, modifier: Modifier = Modifier) {
    // TODO: Show error when failed to load
    // TODO: Show loading indicator
    AsyncImage(
        modifier = modifier.clickable { /* Prevents opening post card */ },
        model = ImageRequest.Builder(LocalContext.current)
            .data(mediaUrl)
            .crossfade(true)
            .build(),
        contentScale = ContentScale.FillWidth,
        contentDescription = stringResource(id = R.string.loaded_media)
    )
}

@Composable
private fun ShowMediaCard(onClick: () -> Unit) {
    BorderedCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(6f)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.clickable(onClick = onClick),
                text = stringResource(id = R.string.click_to_show_media),
            )
        }
    }
}

@Composable
private fun PostCardContentBase(
    replyToName: String?,
    replyRelayHint: String?,
    relays: List<String>?,
    content: String,
    isCurrent: Boolean,
    onNavigateToThread: () -> Unit,
) {
    replyToName?.let { ReplyingTo(name = it, replyRelayHint = replyRelayHint) }
    relays?.let { InRelays(relays = it) }
    Spacer(Modifier.height(spacing.medium))
    HyperlinkedText(
        text = content,
        maxLines = if (isCurrent) null else 12,
        onClickNonLink = onNavigateToThread
    )
}

@Composable
private fun PostCardProfilePicture(
    pictureUrl: String,
    pubkey: String,
    trustType: TrustType,
    onOpenProfile: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ProfilePicture(
        modifier = modifier,
        pictureUrl = pictureUrl,
        pubkey = pubkey,
        trustType = trustType,
        onOpenProfile = if (onOpenProfile != null) {
            { onOpenProfile(pubkey) }
        } else {
            null
        }
    )
}

@Composable
private fun PostCardHeader(
    name: String,
    pubkey: String,
    createdAt: Long,
    onOpenProfile: ((String) -> Unit)?
) {
    Row {
        Username(username = name, pubkey = pubkey, onOpenProfile = onOpenProfile)
        Spacer(modifier = Modifier.width(spacing.medium))
        Bullet()
        Spacer(modifier = Modifier.width(spacing.medium))
        RelativeTime(from = createdAt)
    }
}

@Composable
fun PostNotFound() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenEdge)
            .padding(top = spacing.screenEdge)
            .clip(shapes.medium)
            .border(width = spacing.tiny, color = Color.DarkGray, shape = shapes.medium)
            .background(Color.LightGray)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.screenEdge),
            text = stringResource(id = R.string.post_not_found),
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )
    }
}

@Composable
private fun PostCardActions(
    numOfReplies: Int,
    post: PostWithMeta,
    onLike: () -> Unit,
    onQuote: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToReply: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ReplyAction(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            numOfReplies = numOfReplies,
            postToReplyTo = post,
            onPrepareReply = onPrepareReply,
            onNavigateToReply = onNavigateToReply
        )
        QuoteAction(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onQuote = onQuote
        )
        LikeAction(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            isLikedByMe = post.isLikedByMe,
            onLike = onLike
        )
    }
}

@Composable
private fun ReplyAction(
    numOfReplies: Int,
    postToReplyTo: PostWithMeta,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ReplyIcon(modifier = Modifier
            .size(sizing.smallIcon)
            .clip(RoundedCornerShape(spacing.medium))
            .clickable {
                onPrepareReply(postToReplyTo)
                onNavigateToReply()
            })
        Spacer(Modifier.width(spacing.medium))
        Text(
            text = numOfReplies.toString(),
            color = if (numOfReplies > 0) Color.Unspecified else Color.Transparent
        )
    }
}

@Composable
private fun QuoteAction(
    onQuote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClicked = remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuoteIcon(
            modifier = Modifier
                .size(sizing.smallIcon)
                .clip(RoundedCornerShape(spacing.medium))
                .clickable {
                    onQuote()
                    isClicked.value = true
                }
        )
    }
}

@Composable
private fun LikeAction(
    isLikedByMe: Boolean,
    onLike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClicked = remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconModifier = Modifier
            .size(sizing.smallIcon)
            .clip(RoundedCornerShape(spacing.medium))
        LikeIcon(
            modifier = if (isLikedByMe) iconModifier.clickable { }
            else iconModifier.clickable {
                onLike()
                isClicked.value = true
            },
            isLiked = isLikedByMe || isClicked.value,
        )
    }
}

@Composable
fun NoPostsHint() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        SearchIcon(modifier = Modifier.fillMaxSize(0.1f), tint = Color.LightGray)
        Text(
            text = stringResource(id = R.string.no_posts_found),
            textAlign = TextAlign.Center,
            color = Color.LightGray
        )
    }
}

private fun drawThread(scope: DrawScope, x: Float, yStart: Float, yEnd: Float, width: Float) {
    scope.drawLine(
        color = Color.LightGray,
        start = Offset(x = x, y = yStart),
        end = Offset(x = x, y = yEnd),
        strokeWidth = width
    )
}
