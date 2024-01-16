package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.components.dropdown.SimpleDropdownMenuItem
import com.dluvian.nozzle.ui.components.iconButtons.QuoteIconButton
import com.dluvian.nozzle.ui.components.iconButtons.ReplyIconButton
import com.dluvian.nozzle.ui.components.iconButtons.toggle.LikeToggleIconButton
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun PostCardActions(
    numOfReplies: Int,
    post: PostWithMeta,
    onLike: () -> Unit,
    onDeleteLike: () -> Unit,
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
                    EncodingUtils.createNeventStr(
                        postId = post.entity.id,
                        relays = post.relays
                    ).orEmpty()
                )
            }
        )
        LikeAction(
            modifier = Modifier.weight(1f),
            isLikedByMe = post.isLikedByMe,
            onLike = onLike,
            onDeleteLike = onDeleteLike,
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
        ReplyIconButton(
            modifier = Modifier.size(sizing.smallItem),
            onReply = {
                onPrepareReply(postToReplyTo)
                onNavigateToReply()
            },
            description = stringResource(id = R.string.reply)
        )

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
        QuoteIconButton(
            modifier = Modifier.size(sizing.smallItem),
            onQuote = onNavigateToQuote,
            description = stringResource(R.string.quote)
        )
    }
}

@Composable
private fun LikeAction(
    isLikedByMe: Boolean,
    onLike: () -> Unit,
    onDeleteLike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLiked = remember(isLikedByMe) { mutableStateOf(isLikedByMe) }
    val showDeletePopup = remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LikeToggleIconButton(
            modifier = Modifier.size(sizing.smallItem),
            isLiked = isLiked.value,
            onToggleLike = {
                if (isLiked.value) showDeletePopup.value = true
                else {
                    isLiked.value = true
                    onLike()
                }
            }
        )
        DeleteLikePopup(
            isOpen = showDeletePopup.value,
            onDismiss = { showDeletePopup.value = false },
            onDeleteLike = {
                isLiked.value = false
                onDeleteLike()
            }
        )
    }
}

@Composable
private fun DeleteLikePopup(isOpen: Boolean, onDismiss: () -> Unit, onDeleteLike: () -> Unit) {
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = onDismiss
    ) {
        SimpleDropdownMenuItem(
            text = stringResource(id = R.string.attempt_to_delete_reaction),
            onClick = {
                onDeleteLike()
                onDismiss()
            }
        )
    }
}
