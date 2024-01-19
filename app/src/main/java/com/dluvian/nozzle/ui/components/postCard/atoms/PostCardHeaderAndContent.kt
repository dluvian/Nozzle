package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.utils.copyAndToast
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

@Composable
fun PostCardHeaderAndContent(
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
                    text = EncodingUtils.createNeventStr(
                        postId = post.entity.id,
                        relays = post.relays
                    )
                        .orEmpty(),
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
