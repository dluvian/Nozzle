package com.dluvian.nozzle.ui.components.postCard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ThreadPosition
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardActions
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardHeaderAndContent
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardProfilePicture
import com.dluvian.nozzle.ui.components.postCard.atoms.cards.MediaDecisionCard
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PostCard(
    post: PostWithMeta,
    showProfilePicture: Boolean,
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
            modifier = Modifier
                .size(sizing.profilePicture)
                .semantics(mergeDescendants = true) { this.invisibleToUser() },
            pubkey = post.pubkey,
            picture = post.picture,
            showProfilePicture = showProfilePicture,
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
                    showProfilePicture = showProfilePicture,
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
                onDeleteLike = { postCardLambdas.onDeleteLike(post.entity.id) },
                onPrepareReply = onPrepareReply, // TODO: No prepareReply
                onNavigateToReply = postCardLambdas.navLambdas.onNavigateToReply,
                onNavigateToQuote = postCardLambdas.navLambdas.onNavigateToQuote,
            )
        }
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
