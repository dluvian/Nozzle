package com.dluvian.nozzle.ui.components.postCard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.ui.components.postCard.atoms.BorderedCard
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardContentBase
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardProfilePicture
import com.dluvian.nozzle.ui.components.postCard.molecules.PostCardHeader
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun AnnotatedMentionedPostCard(
    post: AnnotatedMentionedPost,
    modifier: Modifier = Modifier,
    maxLines: Int? = null,
    onNavigateToId: (String) -> Unit,
    onNavigateToProfile: ((String) -> Unit)? = null,
    onNavigateToThread: ((String) -> Unit)? = null,
) {
    Box(modifier = modifier) {
        BorderedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToThread?.let { it(post.mentionedPost.id) } }
        ) {
            Column(modifier = Modifier.padding(spacing.large)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PostCardProfilePicture(
                        modifier = Modifier
                            .size(sizing.smallProfilePicture)
                            .clip(CircleShape),
                        pubkey = post.mentionedPost.pubkey.orEmpty(),
                        trustType = Oneself, // TODO: Find correct trust type
                        onNavigateToProfile = onNavigateToProfile,
                    )
                    Spacer(modifier = Modifier.width(spacing.medium))
                    PostCardHeader(
                        name = if (post.mentionedPost.name.isNullOrEmpty())
                            ShortenedNameUtils.getShortenedNpubFromPubkey(post.mentionedPost.pubkey)
                                .orEmpty()
                        else post.mentionedPost.name,
                        pubkey = post.mentionedPost.pubkey.orEmpty(),
                        createdAt = post.mentionedPost.createdAt ?: 0L,
                        onOpenProfile = onNavigateToProfile,
                        showOptions = false,
                    )
                }
                PostCardContentBase(
                    replyToName = null,
                    replyRelayHint = null,
                    relays = null,
                    annotatedContent = post.annotatedContent,
                    isCurrent = false,
                    maxLines = maxLines,
                    onNavigateToThread = { onNavigateToThread?.let { it(post.mentionedPost.id) } },
                    onNavigateToId = onNavigateToId,
                )
            }
        }
    }
}