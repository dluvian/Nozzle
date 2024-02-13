package com.dluvian.nozzle.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.helper.extended.PostEntityExtended

@Immutable
data class PostWithMeta(
    val entity: PostEntity,
    val replyToName: String?,
    val replyToPubkey: String?,
    val pubkey: Pubkey,
    val annotatedContent: AnnotatedString,
    val mediaUrls: List<String>,
    val annotatedMentionedPosts: List<AnnotatedMentionedPost>,
    val name: String,
    val picture: String?,
    val isLikedByMe: Boolean,
    val isFollowedByMe: Boolean,
    val isOneself: Boolean,
    val trustScore: Float?,
    val numOfReplies: Int,
    val relays: List<String>,
    val hasUnknownAuthor: Boolean,
) : Identifiable {
    override fun getId() = entity.id

    companion object {
        fun from(
            extendedPostEntity: PostEntityExtended,
            relays: List<Relay>,
            isOneself: Boolean,
            isFollowedByMe: Boolean,
            trustScore: Float?,
            annotatedContent: AnnotatedString,
            mediaUrls: List<String>,
            annotatedMentionedPosts: List<AnnotatedMentionedPost>
        ): PostWithMeta {
            val pubkey = extendedPostEntity.postEntity.pubkey

            return PostWithMeta(
                entity = extendedPostEntity.postEntity,
                pubkey = pubkey,
                name = extendedPostEntity.name.orEmpty()
                    .ifEmpty { ShortenedNameUtils.getShortenedNpubFromPubkey(pubkey).orEmpty() },
                picture = extendedPostEntity.picture,
                replyToPubkey = extendedPostEntity.replyToPubkey,
                replyToName = getReplyToName(post = extendedPostEntity),
                isLikedByMe = extendedPostEntity.isLikedByMe,
                numOfReplies = extendedPostEntity.numOfReplies,
                relays = relays,
                isOneself = isOneself,
                isFollowedByMe = isFollowedByMe,
                trustScore = trustScore,
                annotatedContent = annotatedContent,
                mediaUrls = mediaUrls,
                annotatedMentionedPosts = annotatedMentionedPosts,
                hasUnknownAuthor = extendedPostEntity.name == null
            )
        }

        private fun getReplyToName(post: PostEntityExtended): String? {
            return if (post.replyToPubkey != null) {
                post.replyToName.orEmpty().ifEmpty {
                    ShortenedNameUtils.getShortenedNpubFromPubkey(post.replyToPubkey)
                }
            } else if (post.postEntity.replyToId != null) ""
            else null
        }
    }
}
