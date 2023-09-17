package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.postIdToNostrId
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.helper.BasePost
import com.dluvian.nozzle.data.room.helper.ReplyContext
import com.dluvian.nozzle.data.subscriber.IMentionSubscriber
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private const val TAG = "ThreadProvider"

class ThreadProvider(
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val nostrSubscriber: INostrSubscriber,
    private val mentionSubscriber: IMentionSubscriber,
    private val postDao: PostDao,
) : IThreadProvider {
    override suspend fun getThreadFlow(
        postId: String,
        relays: List<String>?,
        waitForSubscription: Long?
    ): Flow<PostThread> {
        val recommendedRelays = mutableListOf<String>()
        val hexId = postIdToNostrId(postId)?.let {
            recommendedRelays.addAll(it.getRecommendedRelays())
            it.getHex()
        } ?: postId

        renewThreadSubscription(
            currentPostId = hexId,
            relays = relays?.let { it + recommendedRelays }
        )
        // TODO: Use a channel
        waitForSubscription?.let { delay(it) }

        val replyContextList = postDao.listReplyContext(currentPostId = hexId)
        val current = replyContextList.find { it.id == hexId }
            ?: return flow { emit(PostThread.createEmpty()) }
        val replies = replyContextList.filter { it.replyToId == current.id }
        val previous = listPrevious(current = current)

        val allPosts = (replyContextList + previous).map {
            BasePost(
                id = it.id,
                pubkey = it.pubkey,
                content = it.content
            )
        }

        val mentionedPubkeysAndAuthorPubkeys = mentionSubscriber
            .subscribeMentionedProfiles(basePosts = allPosts)
        val mentionedPosts = mentionSubscriber.subscribeMentionedPosts(basePosts = allPosts)

        return getMappedThreadFlow(
            currentId = current.id,
            previousIds = previous.map { it.id },
            replyIds = replies.map { it.id },
            authorPubkeys = mentionedPubkeysAndAuthorPubkeys.authorPubkeys,
            mentionedPubkeys = mentionedPubkeysAndAuthorPubkeys.pubkeys,
            mentionedPostIds = mentionedPosts.map { it.eventId }
        )
    }

    private fun renewThreadSubscription(
        currentPostId: String,
        relays: List<String>?
    ) {
        nostrSubscriber.unsubscribeThread()
        nostrSubscriber.subscribeThread(
            currentPostId = currentPostId,
            relays = relays
        )
    }

    private suspend fun listPrevious(current: ReplyContext): List<ReplyContext> {
        if (current.replyToId == null) return emptyList()

        val previous = mutableListOf(current)
        while (true) {
            val currentReplyToId = previous.last().replyToId ?: break
            val previousReplyContext = postDao.getReplyContext(id = currentReplyToId)
            if (previousReplyContext == null) break
            else previous.add(previousReplyContext)
        }

        previous.reverse() // Root first
        previous.removeLast() // Removing 'current'

        return previous
    }

    private suspend fun getMappedThreadFlow(
        currentId: String,
        previousIds: List<String>, // Order is important
        replyIds: List<String>,
        authorPubkeys: Collection<String>,
        mentionedPubkeys: Collection<String>,
        mentionedPostIds: Collection<String>,
    ): Flow<PostThread> {
        val relevantPostIds = listOf(listOf(currentId), previousIds, replyIds).flatten()
        return postWithMetaProvider.getPostsWithMetaFlow(
            postIds = relevantPostIds,
            authorPubkeys = authorPubkeys,
            mentionedPubkeys = mentionedPubkeys,
            mentionedPostIds = mentionedPostIds,
        )
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
            .map { unsortedPosts ->
                val currentPost = unsortedPosts.find { currentId == it.entity.id }
                PostThread(
                    current = currentPost,
                    previous = unsortedPosts
                        .filter { unsorted -> previousIds.any { it == unsorted.entity.id } }
                        .sortedBy { unsorted ->
                            previousIds.indexOfFirst { previousId ->
                                unsorted.entity.id == previousId
                            }
                        },
                    replies = sortReplies(
                        replies = unsortedPosts.filter { unsorted ->
                            replyIds.any { replyId -> replyId == unsorted.entity.id }
                        },
                        originalAuthor = currentPost?.pubkey.orEmpty().ifEmpty {
                            Log.w(TAG, "Failed to find current post in thread")
                            ""
                        }
                    )
                )
            }
    }

    private fun sortReplies(
        replies: Collection<PostWithMeta>,
        originalAuthor: String
    ): List<PostWithMeta> {
        return replies.sortedBy { reply ->
            if (reply.pubkey == originalAuthor) 1
            else if (reply.isFollowedByMe || reply.isOneself) 2
            else if ((reply.trustScore ?: 0f) > 0f) 3
            else 4
        }
    }
}
