package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.utils.IdExtractorUtils.extractNeventsAndNoteIds
import com.dluvian.nozzle.data.nostr.utils.IdExtractorUtils.extractNprofilesAndNpubs
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.helper.ReplyContext
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.helper.IdsAndPubkeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private const val TAG = "ThreadProvider"

class ThreadProvider(
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val nostrSubscriber: INostrSubscriber,
    private val postDao: PostDao,
) : IThreadProvider {
    override suspend fun getThreadFlow(
        currentPostId: String,
        relays: List<String>?,
        waitForSubscription: Long?
    ): Flow<PostThread> {
        renewThreadSubscription(
            currentPostId = currentPostId,
            relays = relays
        )
        // TODO: Use a channel
        waitForSubscription?.let { delay(it) }

        val replyContextList = postDao.listReplyContext(currentPostId = currentPostId)
        val current = replyContextList.find { it.id == currentPostId }
            ?: return flow { emit(PostThread.createEmpty()) }
        val replies = replyContextList.filter { it.replyToId == current.id }
        val previous = listPrevious(currentId = current.id, replyToId = current.replyToId)

        // TODO: Refactor! Same in feedProvider
        val contents = replyContextList.map { it.content }
        val mentionedNprofiles = extractNprofilesAndNpubs(contents = contents)
        mentionedNprofiles.forEach {
            nostrSubscriber.subscribeProfile(
                pubkey = it.pubkey,
                relays = it.relays.ifEmpty { relays }
            )
        }
        // TODO: Refactor! Same in feedProvider
        val mentionedPosts = extractNeventsAndNoteIds(contents = contents)
        mentionedPosts.forEach {
            nostrSubscriber.subscribePost(
                postId = it.eventId,
                relays = it.relays.ifEmpty { relays })
        }

        return getMappedThreadFlow(
            currentId = current.id,
            previousIds = previous.ids,
            replyIds = replies.map { it.id },
            authorPubkeys = replyContextList.map { it.pubkey }.toSet() + previous.pubkeys,
            mentionedPubkeys = mentionedNprofiles.map { it.pubkey },
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

    private suspend fun listPrevious(currentId: String, replyToId: String?): IdsAndPubkeys {
        if (replyToId == null) return IdsAndPubkeys()

        val first = ReplyContext(id = currentId, replyToId = replyToId, pubkey = "")
        val previous = mutableListOf(first)
        while (previous.last().replyToId != null) {
            val currentReplyToId = previous.last().replyToId ?: break
            val previousReplyToIdAndPubkey = postDao.getReplyToIdAndPubkey(id = currentReplyToId)
            val mapped = ReplyContext(
                id = currentReplyToId,
                replyToId = previousReplyToIdAndPubkey?.replyToId,
                pubkey = previousReplyToIdAndPubkey?.pubkey.orEmpty()
            )
            previous.add(mapped)
        }

        previous.reverse() // Root first
        previous.removeLast() // Removing 'current'

        return IdsAndPubkeys(
            ids = previous.map { it.id },
            pubkeys = previous.filter { it.pubkey.isNotEmpty() }.map { it.pubkey })
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
