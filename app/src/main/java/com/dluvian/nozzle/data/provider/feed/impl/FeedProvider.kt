package com.dluvian.nozzle.data.provider.feed.impl

import android.util.Log
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.provider.feed.IInboxFeedProvider
import com.dluvian.nozzle.data.provider.feed.ILikeFeedProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.AuthorSelection
import com.dluvian.nozzle.model.Contacts
import com.dluvian.nozzle.model.Everyone
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.ListAndNumberFlow
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.SingleAuthor
import com.dluvian.nozzle.model.UserSpecific
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

private const val TAG = "FeedProvider"

class FeedProvider(
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val nozzleSubscriber: INozzleSubscriber,
    private val contactListProvider: IContactListProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val postDao: PostDao,
    private val reactionDao: ReactionDao,
) : IFeedProvider, IInboxFeedProvider, ILikeFeedProvider {

    override suspend fun getFeedFlow(
        feedSettings: FeedSettings,
        limit: Int,
        until: Long,
        waitForSubscription: Long,
    ): ListAndNumberFlow<PostWithMeta> {
        Log.i(TAG, "Get feed")
        val authorSelectionPubkeys = listPubkeys(authorSelection = feedSettings.authorSelection)
        nozzleSubscriber.subscribeToFeedPosts(
            isReplies = feedSettings.isReplies,
            hashtag = feedSettings.hashtag,
            authorPubkeys = authorSelectionPubkeys,
            limit = 2 * limit,
            relaySelection = feedSettings.relaySelection,
            until = until
        )
        delay(waitForSubscription)

        val relays = if (feedSettings.relaySelection is UserSpecific) null
        else feedSettings.relaySelection.selectedRelays

        // Params like in getMainFeedBasePosts(..)
        val numOfNewPostsFlow = postDao.getNumOfNewMainFeedPostsFlow(
            isPosts = feedSettings.isPosts,
            isReplies = feedSettings.isReplies,
            hashtag = feedSettings.hashtag,
            authorPubkeys = authorSelectionPubkeys,
            relays = relays,
            until = until,
        )

        val posts = postDao.getMainFeedBasePosts(
            isPosts = feedSettings.isPosts,
            isReplies = feedSettings.isReplies,
            hashtag = feedSettings.hashtag,
            authorPubkeys = authorSelectionPubkeys,
            relays = relays,
            until = until,
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

        // Params like in getInboxPosts(..)
        val numOfNewPostsFlow = postDao.getNumOfNewInboxPostsFlow(
            relays = relays,
            until = until,
        )
        val posts = postDao.getInboxPosts(
            relays = relays,
            until = until,
            limit = limit
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
        nozzleSubscriber.subscribeToPosts(postIds = postIdsToSub)

        delay(waitForSubscription)

        // Params like in getLikedPosts(..)
        val numOfNewPostsFlow = postDao.getNumOfNewLikedPostsFlow(until = until)
        val posts = postDao.getLikedPosts(
            until = until,
            limit = limit,
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

    private fun listPubkeys(authorSelection: AuthorSelection): List<String>? {
        return when (authorSelection) {
            is Everyone -> null
            is Contacts -> contactListProvider.listPersonalContactPubkeysOrDefault() + pubkeyProvider.getActivePubkey()
            is SingleAuthor -> listOf(authorSelection.pubkey)
        }
    }
}
