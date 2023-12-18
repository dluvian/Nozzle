package com.dluvian.nozzle.ui.components.postCard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNeventStr
import com.dluvian.nozzle.data.utils.copyAndToast
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ThreadPosition
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.LikeIcon
import com.dluvian.nozzle.ui.components.QuoteIcon
import com.dluvian.nozzle.ui.components.ReplyIcon
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardContentBase
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardProfilePicture
import com.dluvian.nozzle.ui.components.postCard.molecules.MediaDecisionCard
import com.dluvian.nozzle.ui.components.postCard.molecules.PostCardHeader
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: PostWithMeta,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false,
    threadPosition: ThreadPosition = ThreadPosition.SINGLE,
) {
    val x = sizing.profilePicture / 2 + spacing.screenEdge
    val yTop = spacing.screenEdge
    val yBottom = sizing.profilePicture + spacing.screenEdge
    val small = spacing.small
    Row(modifier
        .combinedClickable(
            enabled = !isCurrent,
            onClick = { postCardLambdas.navLambdas.onNavigateToThread(post.entity.id) })
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
            pubkey = post.pubkey,
            trustType = TrustType.determineTrustType(
                isOneself = post.isOneself,
                isFollowed = post.isFollowedByMe,
                trustScore = post.trustScore,
            ),
            onNavigateToProfile = postCardLambdas.navLambdas.onNavigateToProfile,
        )
        Spacer(Modifier.width(spacing.large))
        Column {
            PostCardHeaderAndContent(
                post = post,
                isCurrent = isCurrent,
                postCardLambdas = postCardLambdas,
            )

            post.mediaUrls.forEach { mediaUrl ->
                Spacer(Modifier.height(spacing.medium))
                MediaDecisionCard(
                    modifier = Modifier.fillMaxWidth(),
                    mediaUrl = mediaUrl,
                    onShowMedia = postCardLambdas.onShowMedia,
                    onShouldShowMedia = postCardLambdas.onShouldShowMedia,
                )
            }

            post.annotatedMentionedPosts.forEach { mentionedPost ->
                Spacer(Modifier.height(spacing.medium))
                AnnotatedMentionedPostCard(
                    post = mentionedPost,
                    onNavigateToProfile = postCardLambdas.navLambdas.onNavigateToProfile,
                    onNavigateToThread = postCardLambdas.navLambdas.onNavigateToThread,
                    onNavigateToId = postCardLambdas.navLambdas.onNavigateToId,
                )
            }

            Spacer(Modifier.height(spacing.large))
            PostCardActions(
                modifier = Modifier.fillMaxWidth(0.92f),
                numOfReplies = post.numOfReplies,
                post = post,
                onLike = { postCardLambdas.onLike(post) },
                onPrepareReply = onPrepareReply, // TODO: No prepareReply
                onNavigateToReply = postCardLambdas.navLambdas.onNavigateToReply,
                onNavigateToQuote = postCardLambdas.navLambdas.onNavigateToQuote,
            )
        }
    }
}

@Composable
private fun PostCardHeaderAndContent(
    post: PostWithMeta,
    isCurrent: Boolean,
    postCardLambdas: PostCardLambdas,
) {
    val context = LocalContext.current
    val clip = LocalClipboardManager.current
    Column {
        PostCardHeader(
            name = post.name,
            pubkey = post.pubkey,
            createdAt = post.entity.createdAt,
            onOpenProfile = postCardLambdas.navLambdas.onNavigateToProfile,
            showOptions = true,
            onCopyId = {
                copyAndToast(
                    text = createNeventStr(postId = post.entity.id, relays = post.relays).orEmpty(),
                    toast = context.getString(R.string.copied_note_id),
                    context = context,
                    clip = clip
                )
            },
            onCopyContent = {
                copyAndToast(
                    text = post.entity.content,
                    toast = context.getString(R.string.copied_content),
                    context = context,
                    clip = clip
                )
            },
            onFollow = if (post.isFollowedByMe || post.isOneself) null else {
                { postCardLambdas.onFollow(post.pubkey) }
            },
            onUnfollow = if (!post.isFollowedByMe || post.isOneself) null else {
                { postCardLambdas.onUnfollow(post.pubkey) }
            },
            onDelete = if (post.isOneself) {
                { postCardLambdas.onDelete(post.entity.id) }
            } else null
        )
        PostCardContentBase(
            replyToName = post.replyToName,
            replyRelayHint = post.entity.replyRelayHint,
            relays = post.relays,
            annotatedContent = post.annotatedContent,
            isCurrent = isCurrent,
            onNavigateToThread = {
                if (!isCurrent) {
                    postCardLambdas.navLambdas.onNavigateToThread(post.entity.id)
                }
            },
            onNavigateToId = postCardLambdas.navLambdas.onNavigateToId,
        )
    }
}

@Composable
private fun PostCardActions(
    numOfReplies: Int,
    post: PostWithMeta,
    onLike: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ReplyAction(
            modifier = Modifier.weight(1f),
            numOfReplies = numOfReplies,
            postToReplyTo = post,
            onPrepareReply = onPrepareReply,
            onNavigateToReply = onNavigateToReply
        )
        QuoteAction(
            modifier = Modifier.weight(1f),
            onNavigateToQuote = {
                onNavigateToQuote(
                    createNeventStr(
                        postId = post.entity.id,
                        relays = post.relays
                    ).orEmpty()
                )
            }
        )
        LikeAction(
            modifier = Modifier.weight(1f),
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
            .size(sizing.smallItem)
            .clip(RoundedCornerShape(spacing.medium))
            .clickable {
                onPrepareReply(postToReplyTo)
                onNavigateToReply()
            })
        if (numOfReplies > 0) {
            Spacer(Modifier.width(spacing.medium))
            Text(text = numOfReplies.toString())
        }
    }
}

@Composable
private fun QuoteAction(
    modifier: Modifier = Modifier,
    onNavigateToQuote: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuoteIcon(
            modifier = Modifier
                .size(sizing.smallItem)
                .clip(RoundedCornerShape(spacing.medium))
                .clickable(onClick = onNavigateToQuote)
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
            .size(sizing.smallItem)
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

private fun drawThread(scope: DrawScope, x: Float, yStart: Float, yEnd: Float, width: Float) {
    scope.drawLine(
        color = Color.LightGray,
        start = Offset(x = x, y = yStart),
        end = Offset(x = x, y = yEnd),
        strokeWidth = width
    )
}
