package com.dluvian.nozzle.data.provider.impl

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.data.RESUB_AFTER
import com.dluvian.nozzle.data.annotatedContent.IAnnotatedContentHandler
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.helper.extended.PostEntityExtended
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.utils.LONG_DEBOUNCE
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.SHORT_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.helper.FeedInfo
import com.dluvian.nozzle.model.helper.MentionedNamesAndPosts
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicInteger

class PostWithMetaProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val contactListProvider: IContactListProvider,
    private val annotatedContentHandler: IAnnotatedContentHandler,
    private val nozzleSubscriber: INozzleSubscriber,
    private val postDao: PostDao,
    private val eventRelayDao: EventRelayDao,
    private val contactDao: ContactDao,
    private val profileDao: ProfileDao,
) : IPostWithMetaProvider {
    override suspend fun getPostsWithMetaFlow(feedInfo: FeedInfo): Flow<List<PostWithMeta>> {
        if (feedInfo.postIds.isEmpty()) return flow { emit(emptyList()) }

        // TODO: No PersonalPubkey. Use extra DB Table for current user
        val extendedPostsFlow = postDao
            .listExtendedPostsFlow(
                postIds = feedInfo.postIds,
                personalPubkey = pubkeyProvider.getPubkey()
            )
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        // TODO: JOIN isFollowedByMe
        val contactPubkeysFlow = contactListProvider
            .getPersonalContactPubkeysFlow()
            .firstThenDistinctDebounce(SHORT_DEBOUNCE)

        // TODO: In initial SQL query possible?
        val relaysFlow = eventRelayDao
            .getRelaysPerEventIdMapFlow(feedInfo.postIds)
            .firstThenDistinctDebounce(LONG_DEBOUNCE)

        // TODO: No pubkey. Use extra DB Table for current user
        val trustScorePerPubkeyFlow = contactDao
            .getTrustScoreByPubkeyFlow(
                pubkey = pubkeyProvider.getPubkey(),
                contactPubkeys = feedInfo.authorPubkeys
            )
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        val mentionedNamesAndPostsFlow = getMentionedNamesAndPostsFlow(
            mentionedPubkeys = feedInfo.mentionedPubkeys,
            mentionedPostIds = feedInfo.mentionedPostIds
        ).firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        val intervalCounter = AtomicInteger(0)
        return getFinalFlow(
            extendedPostsFlow = extendedPostsFlow,
            contactPubkeysFlow = contactPubkeysFlow,
            relaysFlow = relaysFlow,
            trustScorePerPubkeyFlow = trustScorePerPubkeyFlow,
            mentionedNamesAndPostsFlow = mentionedNamesAndPostsFlow,
        )
            .distinctUntilChanged()
            .combine(getIntervalFlow()) { posts, count ->
                if (intervalCounter.compareAndSet(count - 1, count)) {
                    nozzleSubscriber.subscribeUnknowns(posts = posts)
                }
                posts
            }

    }

    private fun getMentionedNamesAndPostsFlow(
        mentionedPubkeys: Collection<String>,
        mentionedPostIds: Collection<String>
    ): Flow<MentionedNamesAndPosts> {
        val mentionedNamesFlow = if (mentionedPubkeys.isEmpty()) flow { emit(emptyMap()) }
        else profileDao.getPubkeyToNameMapFlow(pubkeys = mentionedPubkeys)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        val mentionedPostsFlow = if (mentionedPostIds.isEmpty()) flow { emit(emptyMap()) }
        else postDao.getMentionedPostsByIdFlow(postIds = mentionedPostIds)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)

        return combine(mentionedNamesFlow, mentionedPostsFlow) { names, posts ->
            MentionedNamesAndPosts(
                mentionedNamesByPubkey = names,
                mentionedPostsById = posts
            )
        }
    }

    private fun getFinalFlow(
        extendedPostsFlow: Flow<List<PostEntityExtended>>,
        contactPubkeysFlow: Flow<List<String>>,
        relaysFlow: Flow<Map<String, List<String>>>,
        trustScorePerPubkeyFlow: Flow<Map<String, Float>>,
        mentionedNamesAndPostsFlow: Flow<MentionedNamesAndPosts>
    ): Flow<List<PostWithMeta>> {
        return combine(
            extendedPostsFlow,
            contactPubkeysFlow,
            relaysFlow,
            trustScorePerPubkeyFlow,
            mentionedNamesAndPostsFlow
        ) { extended, contacts, relays, trustScore, mentionedNamesAndPosts ->
            extended.map {
                val pubkey = it.postEntity.pubkey
                val isOneself = pubkeyProvider.isOneself(pubkey)
                val annotatedContent = annotatedContentHandler.annotateContent(
                    content = it.postEntity.content,
                    mentionedPubkeyToName = mentionedNamesAndPosts.mentionedNamesByPubkey
                )
                PostWithMeta(
                    entity = it.postEntity,
                    pubkey = pubkey,
                    name = it.name.orEmpty()
                        .ifEmpty { getShortenedNpubFromPubkey(pubkey).orEmpty() },
                    pictureUrl = it.pictureUrl.orEmpty(),
                    replyToPubkey = it.replyToPubkey,
                    replyToName = getReplyToName(it),
                    isLikedByMe = it.isLikedByMe,
                    numOfReplies = it.numOfReplies,
                    relays = relays[it.postEntity.id].orEmpty(),
                    isOneself = isOneself,
                    isFollowedByMe = if (isOneself) false else contacts.contains(pubkey),
                    trustScore = if (isOneself) null else trustScore[pubkey],
                    annotatedContent = annotatedContent,
                    mediaUrls = annotatedContentHandler.extractMediaLinks(annotatedContent),
                    mentionedPosts = getMentionedPosts(
                        annotatedContent = annotatedContent,
                        mentionedPostsById = mentionedNamesAndPosts.mentionedPostsById
                    ),
                )
            }
        }
    }

    private fun getIntervalFlow(): Flow<Int> {
        return flow {
            emit(0)
            delay(RESUB_AFTER)
            emit(1)
        }
    }

    private fun getMentionedPosts(
        annotatedContent: AnnotatedString,
        mentionedPostsById: Map<String, MentionedPost>
    ): List<MentionedPost> {
        return annotatedContentHandler.extractNevents(annotatedContent)
            .map { nevent ->
                mentionedPostsById[nevent.eventId]?.let { post ->
                    if (post.name.isNullOrEmpty()) {
                        post.copy(name = getShortenedNpubFromPubkey(post.pubkey))
                    } else post
                }
                    ?: MentionedPost(
                        id = nevent.eventId,
                        pubkey = "",
                        content = "",
                        name = "",
                        picture = "",
                        createdAt = 0L
                    )
            }
    }

    private fun getReplyToName(post: PostEntityExtended): String? {
        return if (post.replyToPubkey != null) {
            post.replyToName.orEmpty().ifEmpty {
                getShortenedNpubFromPubkey(post.replyToPubkey)
            }
        } else if (post.postEntity.replyToId != null) ""
        else null
    }
}
