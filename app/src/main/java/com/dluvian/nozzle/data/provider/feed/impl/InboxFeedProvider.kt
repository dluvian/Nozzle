package com.dluvian.nozzle.data.provider.feed.impl

import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.feed.IInboxFeedProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InboxFeedProvider(
    private val nozzleSubscriber: INozzleSubscriber,
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val postDao: PostDao,
) : IInboxFeedProvider {
    override suspend fun getInboxFeedFlow(
        relays: Collection<String>,
        limit: Int,
        until: Long,
        waitForSubscription: Long
    ): Flow<List<PostWithMeta>> {
        if (relays.isEmpty() || limit <= 0) return flowOf(emptyList())

        nozzleSubscriber.subscribeToInbox(
            relays = relays,
            limit = limit,
            until = until,
        )
        delay(waitForSubscription)

        val posts = postDao.getInboxBasePosts(
            relays = relays,
            until = until,
            limit = limit
        )
        val feedInfo = nozzleSubscriber.subscribeFeedInfo(posts = posts)

        return postWithMetaProvider.getPostsWithMetaFlow(feedInfo = feedInfo)
    }
}
