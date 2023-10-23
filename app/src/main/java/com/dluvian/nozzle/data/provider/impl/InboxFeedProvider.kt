package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.provider.IInboxFeedProvider
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InboxFeedProvider(
    private val nozzleSubscriber: INozzleSubscriber,
    private val pubkeyProvider: IPubkeyProvider,
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val postDao: PostDao,
) : IInboxFeedProvider {
    override suspend fun getInboxFeedFlow(
        relays: Collection<String>,
        limit: Int,
        until: Long,
        waitForSubscription: Long
    ): Flow<List<PostWithMeta>> {
        if (relays.isEmpty() || limit <= 0) return flow { emit(emptyList()) }

        nozzleSubscriber.subscribeToInbox(
            relays = relays,
            limit = limit,
            until = until,
        )
        delay(waitForSubscription)

        val posts = postDao.getInboxBasePosts(
            mentionedPubkey = pubkeyProvider.getActivePubkey(),
            relays = relays,
            until = until,
            limit = limit
        )
        val feedInfo = nozzleSubscriber.subscribeFeedInfo(posts = posts)

        return postWithMetaProvider.getPostsWithMetaFlow(feedInfo = feedInfo)
    }
}
