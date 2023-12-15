package com.dluvian.nozzle.data.provider.feed.impl

import android.util.Log
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.AuthorSelection
import com.dluvian.nozzle.model.Contacts
import com.dluvian.nozzle.model.Everyone
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RelaySelection
import com.dluvian.nozzle.model.SingleAuthor
import com.dluvian.nozzle.model.UserSpecific
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

private const val TAG = "FeedProvider"

class FeedProvider(
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val nozzleSubscriber: INozzleSubscriber,
    private val postDao: PostDao,
    private val contactListProvider: IContactListProvider,
    private val pubkeyProvider: IPubkeyProvider,
) : IFeedProvider {

    override suspend fun getFeedFlow(
        feedSettings: FeedSettings,
        limit: Int,
        until: Long,
        waitForSubscription: Long,
    ): Flow<List<PostWithMeta>> {
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

        val posts = listPosts(
            isPosts = feedSettings.isPosts,
            isReplies = feedSettings.isReplies,
            hashtag = feedSettings.hashtag,
            authorPubkeys = authorSelectionPubkeys,
            relaySelection = feedSettings.relaySelection,
            until = until,
            limit = limit,
        )

        val feedInfo = nozzleSubscriber.subscribeFeedInfo(posts = posts)

        return postWithMetaProvider.getPostsWithMetaFlow(feedInfo = feedInfo)
    }

    private fun listPubkeys(authorSelection: AuthorSelection): List<String>? {
        return when (authorSelection) {
            is Everyone -> null
            is Contacts -> contactListProvider.listPersonalContactPubkeysOrDefault() + pubkeyProvider.getActivePubkey()
            is SingleAuthor -> listOf(authorSelection.pubkey)
        }
    }

    private suspend fun listPosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: List<String>?,
        relaySelection: RelaySelection,
        until: Long,
        limit: Int,
    ): List<PostEntity> {
        if (!isPosts && !isReplies) return emptyList()
        val relays = if (relaySelection is UserSpecific) null else relaySelection.selectedRelays

        // TODO: Check if nullable Collection can be used in queries. Refac into one query if possible
        return if (authorPubkeys == null && relays == null) {
            Log.d(TAG, "Get global feed")
            postDao.getGlobalFeedBasePosts(
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                until = until,
                limit = limit,
            )
        } else if (authorPubkeys == null && relays != null) {
            Log.d(TAG, "Get global feed by ${relays.size} relays $relays")
            if (relays.isEmpty()) emptyList()
            else postDao.getGlobalFeedBasePostsByRelays(
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                relays = relays,
                until = until,
                limit = limit,
            )
        } else if (authorPubkeys != null && relays == null) {
            Log.d(TAG, "Get ${authorPubkeys.size} authored feed")
            if (authorPubkeys.isEmpty()) emptyList()
            else postDao.getAuthoredFeedBasePosts(
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                authorPubkeys = authorPubkeys,
                until = until,
                limit = limit,
            )
        } else if (authorPubkeys != null && relays != null) {
            Log.d(TAG, "Get ${authorPubkeys.size} authored feed by ${relays.size} relays")
            if (authorPubkeys.isEmpty() || relays.isEmpty()) emptyList()
            else postDao.getAuthoredFeedBasePostsByRelays(
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                authorPubkeys = authorPubkeys,
                relays = relays,
                until = until,
                limit = limit,
            )
        } else {
            Log.w(TAG, "Could not find correct db call. Default to empty list")
            emptyList()
        }
    }
}
