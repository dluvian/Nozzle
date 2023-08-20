package com.dluvian.nozzle.data.mapper

import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.UrlUtils
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.data.utils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.ParsedContent
import com.dluvian.nozzle.model.PostEntityExtended
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.util.Collections

class PostMapper(
    private val pubkeyProvider: IPubkeyProvider,
    private val contactListProvider: IContactListProvider,
    private val postDao: PostDao,
    private val eventRelayDao: EventRelayDao,
    private val contactDao: ContactDao,
) : IPostMapper {
    override suspend fun mapToPostsWithMetaFlow(
        postIds: Collection<String>,
        authorPubkeys: Collection<String>,
    ): Flow<List<PostWithMeta>> {
        if (postIds.isEmpty()) return flow { emit(emptyList()) }

        val extendedPostsFlow = postDao
            .listExtendedPostsFlow(postIds = postIds, personalPubkey = pubkeyProvider.getPubkey())
            .distinctUntilChanged()

        val contactPubkeysFlow = contactListProvider
            .getPersonalContactPubkeysFlow()
            .distinctUntilChanged()

        // TODO: In initial SQL query possible?
        val relaysFlow = eventRelayDao
            .getRelaysPerEventIdMapFlow(postIds)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        // No debounce because of immediate user interaction
        val trustScorePerPubkeyFlow = contactDao
            .getTrustScorePerPubkeyFlow(
                pubkey = pubkeyProvider.getPubkey(),
                contactPubkeys = authorPubkeys
            )
            .distinctUntilChanged()

        return getFinalFlow(
            extendedPostsFlow = extendedPostsFlow,
            contactPubkeysFlow = contactPubkeysFlow,
            relaysFlow = relaysFlow,
            trustScorePerPubkeyFlow = trustScorePerPubkeyFlow,
        ).distinctUntilChanged()
    }

    private fun getFinalFlow(
        extendedPostsFlow: Flow<List<PostEntityExtended>>,
        contactPubkeysFlow: Flow<List<String>>,
        relaysFlow: Flow<Map<String, List<String>>>,
        trustScorePerPubkeyFlow: Flow<Map<String, Float>>,
    ): Flow<List<PostWithMeta>> {
        return combine(
            extendedPostsFlow,
            contactPubkeysFlow,
            relaysFlow,
            trustScorePerPubkeyFlow,
        ) { extended, contacts, relays, trustScore ->
            extended.map {
                val pubkey = it.postEntity.pubkey
                val isOneself = pubkeyProvider.isOneself(pubkey)
                val parsedContent = getParsedContent(it)
                PostWithMeta(
                    id = it.postEntity.id,
                    pubkey = pubkey,
                    createdAt = it.postEntity.createdAt,
                    content = parsedContent.cleanedContent,
                    mediaUrl = parsedContent.mediaUrl,
                    name = it.name.orEmpty().ifEmpty { getShortenedNpubFromPubkey(pubkey) },
                    pictureUrl = it.pictureUrl.orEmpty(),
                    replyToId = it.postEntity.replyToId,
                    replyToPubkey = it.replyToPubkey,
                    replyToName = getReplyToName(it),
                    replyRelayHint = it.postEntity.replyRelayHint,
                    isLikedByMe = it.isLikedByMe,
                    numOfReplies = it.numOfReplies,
                    relays = relays[it.postEntity.id].orEmpty(),
                    isOneself = isOneself,
                    isFollowedByMe = if (isOneself) false else contacts.contains(pubkey),
                    trustScore = if (isOneself) null else trustScore[pubkey],
                    mentionedPost = it.postEntity.mentionedPostId?.let { mentionedPostId ->
                        MentionedPost(
                            id = mentionedPostId,
                            pubkey = it.mentionedPostPubkey.orEmpty(),
                            content = it.mentionedPostContent.orEmpty(),
                            name = it.mentionedPostName
                                .orEmpty()
                                .ifEmpty {
                                    it.mentionedPostPubkey?.let { key ->
                                        getShortenedNpubFromPubkey(key)
                                    }
                                }.orEmpty(),
                            picture = it.mentionedPostPicture.orEmpty(),
                            createdAt = it.mentionedPostCreatedAt ?: 0L,
                        )
                    }
                )
            }
        }
    }

    private fun getReplyToName(post: PostEntityExtended): String? {
        return if (post.replyToPubkey != null) {
            post.replyToName.orEmpty().ifEmpty {
                getShortenedNpubFromPubkey(post.replyToPubkey)
            }
        } else if (post.postEntity.replyToId != null) "???"
        else null
    }

    private val parsedContentCache: MutableMap<String, ParsedContent> =
        Collections.synchronizedMap(mutableMapOf())

    private fun getParsedContent(post: PostEntityExtended): ParsedContent {
        val cached = parsedContentCache[post.postEntity.id]
        if (cached != null) return cached

        val mediaUrl = UrlUtils.getAppendedMediaUrl(post.postEntity.content)
        val cleanedContent = mediaUrl?.let { url ->
            post.postEntity.content.removeSuffix(url).trimEnd()
        } ?: post.postEntity.content
        val result = ParsedContent(cleanedContent = cleanedContent, mediaUrl = mediaUrl)
        parsedContentCache[post.postEntity.id] = result

        return result
    }
}
