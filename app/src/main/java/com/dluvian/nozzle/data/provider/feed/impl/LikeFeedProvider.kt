package com.dluvian.nozzle.data.provider.feed.impl

import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.feed.ILikeFeedProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LikeFeedProvider(
    private val nozzleSubscriber: INozzleSubscriber,
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val postDao: PostDao,
    private val reactionDao: ReactionDao
) : ILikeFeedProvider {
    override suspend fun getLikeFeedFlow(
        limit: Int,
        until: Long,
        waitForSubscription: Long
    ): Flow<List<PostWithMeta>> {
        if (limit <= 0) return flowOf(emptyList())

        nozzleSubscriber.subscribeToLikes(limit = limit, until = until)

        delay(waitForSubscription)

        val postIdsToSub = reactionDao.getMissingEventIds()
        nozzleSubscriber.subscribeToPosts(postIds = postIdsToSub)

        delay(waitForSubscription)

        val posts = postDao.getLikedPosts(until = until, limit = limit)
        val feedInfo = nozzleSubscriber.subscribeFeedInfo(posts = posts)

        return postWithMetaProvider.getPostsWithMetaFlow(feedInfo = feedInfo)
    }
}