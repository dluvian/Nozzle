package com.dluvian.nozzle.ui.components.postCard

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.shapes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.hexToNote
import com.dluvian.nozzle.data.utils.hexToNpub
import com.dluvian.nozzle.model.PostIds
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RepostPreview
import com.dluvian.nozzle.model.ThreadPosition
import com.dluvian.nozzle.ui.components.*
import com.dluvian.nozzle.ui.components.text.*
import com.dluvian.nozzle.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: PostWithMeta,
    onLike: (String) -> Unit,
    onRepost: (String) -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToThread: (PostIds) -> Unit,
    onNavigateToReply: () -> Unit,
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
    Row(
        modifier
            .combinedClickable(
                enabled = !isCurrent,
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
            modifier = Modifier
                .size(sizing.profilePicture)
                .clip(CircleShape),
            pictureUrl = post.pictureUrl,
            pubkey = post.pubkey,
            onOpenProfile = onOpenProfile
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
            Spacer(Modifier.height(spacing.medium))
            RepostCardContent(
                post = post.repost,
                onOpenProfile = onOpenProfile,
                onNavigateToThread = onNavigateToThread,
            )
            Spacer(Modifier.height(spacing.medium))
            PostCardActions(
                numOfReplies = post.numOfReplies,
                numOfReposts = post.numOfReposts,
                numOfLikes = post.numOfLikes,
                post = post,
                onLike = { onLike(post.id) },
                onRepost = { onRepost(post.id) },
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
            name = post.name.ifEmpty { hexToNpub(post.pubkey) },
            pubkey = post.pubkey,
            createdAt = post.createdAt,
            onOpenProfile = onOpenProfile
        )
        PostCardContentBase(
            replyToName = if (post.replyToId != null) {
                post.replyToName.orEmpty()
                    .ifEmpty { post.replyToPubkey.orEmpty().ifEmpty { "???" } }
            } else null,
            relays = post.relays,
            content = post.content,
            isCurrent = isCurrent,
            onNavigateToThread = onNavigateToThread,
        )
    }
}

@Composable
private fun RepostCardContent(
    post: RepostPreview?,
    onOpenProfile: ((String) -> Unit)?,
    onNavigateToThread: (PostIds) -> Unit,
) {
    post?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = spacing.tiny,
                    color = LightGray21,
                    shape = RoundedCornerShape(spacing.large)
                )
                .clickable {
                    onNavigateToThread(it.toPostIds())
                }
        ) {
            Column(modifier = Modifier.padding(spacing.large)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PostCardProfilePicture(
                        modifier = Modifier
                            .size(sizing.smallProfilePicture)
                            .clip(CircleShape),
                        pictureUrl = it.picture,
                        pubkey = it.pubkey,
                        onOpenProfile = onOpenProfile
                    )
                    Spacer(modifier = Modifier.width(spacing.medium))
                    PostCardHeader(
                        name = it.name,
                        pubkey = it.pubkey,
                        createdAt = post.createdAt,
                        onOpenProfile = onOpenProfile
                    )
                }
                PostCardContentBase(
                    replyToName = null,
                    relays = null,
                    content = it.content,
                    isCurrent = false,
                ) {
                    onNavigateToThread(it.toPostIds())
                }
            }
        }
    }
}

@Composable
private fun PostCardContentBase(
    replyToName: String?,
    relays: List<String>?,
    content: String,
    isCurrent: Boolean,
    onNavigateToThread: () -> Unit,
) {
    replyToName?.let { ReplyingTo(name = it) }
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
    onOpenProfile: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ProfilePicture(
        modifier = modifier,
        pictureUrl = pictureUrl,
        pubkey = pubkey,
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
            .border(width = spacing.tiny, color = DarkGray21, shape = shapes.medium)
            .background(LightGray21)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.screenEdge),
            text = stringResource(id = R.string.post_not_found),
            textAlign = TextAlign.Center,
            color = DarkGray21
        )
    }
}

@Composable
private fun PostCardActions(
    numOfReplies: Int,
    numOfReposts: Int,
    numOfLikes: Int,
    post: PostWithMeta,
    onLike: () -> Unit,
    onRepost: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToReply: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ReplyAction(
            numOfReplies = numOfReplies,
            postToReplyTo = post,
            onPrepareReply = onPrepareReply,
            onNavigateToReply = onNavigateToReply
        )
        RepostAction(
            numOfReposts = numOfReposts,
            isRepostedByMe = post.isRepostedByMe,
            onRepost = onRepost
        )
        LikeAction(numOfLikes = numOfLikes, isLikedByMe = post.isLikedByMe, onLike = onLike)
    }
}

@Composable
private fun ReplyAction(
    numOfReplies: Int,
    postToReplyTo: PostWithMeta,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToReply: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ReplyIcon(
            modifier = Modifier
                .size(sizing.smallIcon)
                .clip(CircleShape)
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
private fun RepostAction(
    numOfReposts: Int,
    isRepostedByMe: Boolean,
    onRepost: () -> Unit,
) {
    val isClicked = remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        val modifier = Modifier
            .size(sizing.smallIcon)
            .clip(CircleShape)
        RepostIcon(
            modifier = if (isRepostedByMe) modifier.clickable { }
            else modifier.clickable {
                onRepost()
                isClicked.value = true
            },
            isReposted = isRepostedByMe || isClicked.value
        )
        Spacer(Modifier.width(spacing.medium))
        Text(
            text = if (!isRepostedByMe && isClicked.value) (numOfReposts + 1).toString()
            else numOfReposts.toString(),
            color = if (numOfReposts > 0 || isRepostedByMe || isClicked.value)
                Color.Unspecified else Color.Transparent
        )
    }
}

@Composable
private fun LikeAction(
    numOfLikes: Int,
    isLikedByMe: Boolean,
    onLike: () -> Unit,
) {
    val isClicked = remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        val modifier = Modifier
            .size(sizing.smallIcon)
            .clip(CircleShape)
        LikeIcon(
            modifier = if (isLikedByMe) modifier.clickable { }
            else modifier.clickable {
                onLike()
                isClicked.value = true
            },
            isLiked = isLikedByMe || isClicked.value,
        )
        Spacer(Modifier.width(spacing.medium))
        Text(
            text = if (!isLikedByMe && isClicked.value) (numOfLikes + 1).toString()
            else numOfLikes.toString(),
            color = if (numOfLikes > 0 || isLikedByMe || isClicked.value)
                Color.Unspecified else Color.Transparent
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
        SearchIcon(modifier = Modifier.fillMaxSize(0.1f), tint = LightGray21)
        Text(
            text = stringResource(id = R.string.no_posts_found),
            textAlign = TextAlign.Center,
            color = LightGray21
        )
    }
}

private fun drawThread(scope: DrawScope, x: Float, yStart: Float, yEnd: Float, width: Float) {
    scope.drawLine(
        color = LightGray21,
        start = Offset(x = x, y = yStart),
        end = Offset(x = x, y = yEnd),
        strokeWidth = width
    )
}
