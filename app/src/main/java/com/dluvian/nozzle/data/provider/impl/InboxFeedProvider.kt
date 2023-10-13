package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.provider.IInboxFeedProvider
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class InboxFeedProvider(
    private val nozzleSubscriber: INozzleSubscriber,
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val postDao: PostDao,
) : IInboxFeedProvider {
    override suspend fun getInboxFeedFlow(
        limit: Int,
        until: Long,
        waitForSubscription: Long
    ): Flow<List<PostWithMeta>> {
        nozzleSubscriber.subscribeToInbox(
            limit = limit,
            until = until
        )
        delay(waitForSubscription)

        // TODO: Process
        val posts = postDao.getInboxPosts(until = until, limit = limit)
        val feedInfo = nozzleSubscriber.subscribeFeedInfo(posts = posts)

        return postWithMetaProvider.getPostsWithMetaFlow(feedInfo = feedInfo)
    }
}
