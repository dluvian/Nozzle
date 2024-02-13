package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.postIdToNostrId
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.FeedInfo
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.feedFilter.Autopilot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private const val TAG = "ThreadProvider"

class ThreadProvider(
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val nozzleSubscriber: INozzleSubscriber,
    private val postDao: PostDao,
) : IThreadProvider {
    override suspend fun getThreadFlow(
        postId: String,
        waitForSubscription: Long?
    ): Flow<PostThread> {
        nozzleSubscriber.subscribeThreadPost(postId = postId)
        val hexId = postIdToNostrId(postId)?.hex ?: postId
        val replyContextList = postDao.getPostAndReplies(currentPostId = hexId)
        val current = replyContextList.find { it.id == hexId }
            ?: return flow { emit(PostThread.createEmpty()) }
        val replies = replyContextList.filter { it.replyToId == current.id }
        val previous = listAndSubPrevious(current = current)

        val allPosts = (replies + previous + current)
        val feedInfo = nozzleSubscriber.subscribeFeedInfo(
            posts = allPosts,
            relayFilter = Autopilot
        )

        return getMappedThreadFlow(
            currentId = current.id,
            previousIds = previous.map { it.id },
            feedInfo = feedInfo
        )
    }

    override suspend fun findParents(earliestPost: PostWithMeta) {
        val parents = mutableListOf<PostEntity>()

        var delay = WAIT_TIME
        while (true) {
            val oldestPost = parents.lastOrNull() ?: earliestPost.entity
            val replyToId = oldestPost.replyToId
            if (replyToId == null) {
                Log.i(TAG, "Found oldest parent after finding ${parents.size} parents")
                return
            }
            val inDb = postDao.getPost(id = replyToId)
            if (inDb != null) {
                delay = WAIT_TIME
                parents.add(inDb)
                continue
            }

            nozzleSubscriber.subscribeParentPost(
                noteId = replyToId,
                relayHint = oldestPost.replyRelayHint
            )
            delay(delay)

            val parent = postDao.getPost(id = replyToId)
            if (parent == null) {
                delay += WAIT_TIME
            } else {
                delay = WAIT_TIME
                parents.add(parent)
            }
            if (delay >= 5 * WAIT_TIME) {
                Log.i(TAG, "Failed to find parent after 5 attempts")
                return
            }
        }
    }

    private suspend fun listAndSubPrevious(current: PostEntity): List<PostEntity> {
        if (current.replyToId == null) return emptyList()

        val previous = mutableListOf(current)
        while (true) {
            val currentPost = previous.last()
            val currentReplyToId = currentPost.replyToId ?: break
            val previousPost = postDao.getPost(id = currentReplyToId)
            if (previousPost == null) {
                nozzleSubscriber.subscribeParentPost(
                    noteId = currentReplyToId,
                    relayHint = currentPost.replyRelayHint
                )
                break
            } else previous.add(previousPost)
        }

        previous.reverse() // Root first
        previous.removeLast() // Removing 'current'

        return previous
    }

    private suspend fun getMappedThreadFlow(
        currentId: NoteId,
        previousIds: List<NoteId>, // Order is important
        feedInfo: FeedInfo
    ): Flow<PostThread> {
        val personalReplies = postWithMetaProvider.getPersonalRepliesWithMetaFlow(
            currentId = currentId
        )
        return postWithMetaProvider.getPostsWithMetaFlow(
            feedInfo = feedInfo,
            relayFilter = Autopilot
        ).combine(personalReplies) { posts, myReplies ->
            // Adding replies for when we just replied
            (posts + myReplies).distinctBy { it.entity.id }
        }
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
                            unsorted.entity.replyToId == currentId
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
