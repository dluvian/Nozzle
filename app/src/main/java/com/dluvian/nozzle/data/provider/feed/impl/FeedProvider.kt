package com.dluvian.nozzle.data.provider.feed.impl

import android.util.Log
import com.dluvian.nozzle.data.feedFilterResolver.IFeedFilterResolver
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.provider.feed.IInboxFeedProvider
import com.dluvian.nozzle.data.provider.feed.ILikeFeedProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.ListAndNumberFlow
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.feedFilter.Autopilot
import com.dluvian.nozzle.model.feedFilter.FeedFilter
import com.dluvian.nozzle.model.feedFilter.SingularPerson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

private const val TAG = "FeedProvider"

class FeedProvider(
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val nozzleSubscriber: INozzleSubscriber,
    private val feedFilterResolver: IFeedFilterResolver,
    private val postDao: PostDao,
    private val reactionDao: ReactionDao,
) : IFeedProvider, IInboxFeedProvider, ILikeFeedProvider {

    override suspend fun getFeedFlow(
        feedFilter: FeedFilter,
        limit: Int,
        until: Long,
        waitForSubscription: Long,
    ): ListAndNumberFlow<PostWithMeta> {
        Log.i(TAG, "Get feed, hashtag=${feedFilter.hashtag}")
        val pubkeysByRelay = feedFilterResolver.getPubkeysByRelay(feedFilter = feedFilter)
        nozzleSubscriber.subscribeToFeed(
            pubkeysByRelay = pubkeysByRelay,
            hashtag = feedFilter.hashtag,
            limit = 2 * limit,
            until = until
        )

        delay(waitForSubscription)

        val relaysAreIrrelevant = feedFilter.relayFilter is Autopilot ||
                feedFilter.authorFilter is SingularPerson
        val relays = if (relaysAreIrrelevant) null else pubkeysByRelay.keys
        val authorPubkeys = feedFilterResolver.getPubkeys(authorFilter = feedFilter.authorFilter)

        val posts = postDao.getMainFeedBasePosts(
            isPosts = feedFilter.isPosts,
            isReplies = feedFilter.isReplies,
            hashtag = feedFilter.hashtag,
            authorPubkeys = authorPubkeys,
            relays = relays,
            until = until,
            limit = limit,
        )

        // Params like in getMainFeedBasePosts(..)
        val numOfNewPostsFlow = postDao.getNumOfNewMainFeedPostsFlow(
            oldPostIds = posts.map { it.id },
            isPosts = feedFilter.isPosts,
            isReplies = feedFilter.isReplies,
            hashtag = feedFilter.hashtag,
            authorPubkeys = authorPubkeys,
            relays = relays,
            limit = limit,
        )

        return getResult(posts = posts, numOfNewPostsFlow = numOfNewPostsFlow)
    }

    override suspend fun getInboxFeedFlow(
        relays: Collection<String>,
        limit: Int,
        until: Long,
        waitForSubscription: Long
    ): ListAndNumberFlow<PostWithMeta> {
        if (relays.isEmpty() || limit <= 0) return ListAndNumberFlow()

        nozzleSubscriber.subscribeToInbox(
            relays = relays,
            limit = limit,
            until = until,
        )
        delay(waitForSubscription)

        val posts = postDao.getInboxPosts(
            relays = relays,
            until = until,
            limit = limit
        )
        val numOfNewPostsFlow = postDao.getNumOfNewInboxPostsFlow(
            oldPostIds = posts.map { it.id },
            relays = relays,
            limit = limit,
        )

        return getResult(posts = posts, numOfNewPostsFlow = numOfNewPostsFlow)
    }

    override suspend fun getLikeFeedFlow(
        limit: Int,
        until: Long,
        waitForSubscription: Long
    ): ListAndNumberFlow<PostWithMeta> {
        if (limit <= 0) return ListAndNumberFlow()

        nozzleSubscriber.subscribeToLikes(limit = limit, until = until)

        delay(waitForSubscription)

        val postIdsToSub = reactionDao.getMissingEventIds()
        nozzleSubscriber.subscribeToNotes(noteIds = postIdsToSub)

        delay(waitForSubscription)

        val posts = postDao.getLikedPosts(
            until = until,
            limit = limit,
        )
        val numOfNewPostsFlow = postDao.getNumOfNewLikedPostsFlow(
            oldPostIds = posts.map { it.id },
            limit = limit
        )

        return getResult(posts = posts, numOfNewPostsFlow = numOfNewPostsFlow)
    }

    private suspend fun getResult(
        posts: List<PostEntity>,
        numOfNewPostsFlow: Flow<Int>
    ): ListAndNumberFlow<PostWithMeta> {
        if (posts.isEmpty()) return ListAndNumberFlow()
        val feedInfo = nozzleSubscriber.subscribeFeedInfo(posts = posts)

        return ListAndNumberFlow(
            listFlow = postWithMetaProvider.getPostsWithMetaFlow(feedInfo = feedInfo),
            numFlow = numOfNewPostsFlow
        )
    }
}
