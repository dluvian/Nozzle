package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.mapper.IPostMapper
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.AuthorSelection
import com.dluvian.nozzle.model.Contacts
import com.dluvian.nozzle.model.Everyone
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RelaySelection
import com.dluvian.nozzle.model.SingleAuthor
import com.dluvian.nozzle.model.UserSpecific
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "FeedProvider"

class FeedProvider(
    private val postMapper: IPostMapper,
    private val nostrSubscriber: INostrSubscriber,
    private val postDao: PostDao,
    private val contactListProvider: IContactListProvider,
) : IFeedProvider {

    override suspend fun getFeedFlow(
        feedSettings: FeedSettings,
        limit: Int,
        until: Long?,
        waitForSubscription: Long?,
    ): Flow<List<PostWithMeta>> {
        Log.i(TAG, "Get feed")

        nostrSubscriber.unsubscribeFeeds()
        nostrSubscriber.unsubscribeAdditionalPostsData()
        val authorPubkeys = listPubkeys(authorSelection = feedSettings.authorSelection)
        subscribeToFeed(
            authorPubkeys = authorPubkeys,
            isReplies = feedSettings.isReplies,
            limit = limit,
            until = until,
            relaySelection = feedSettings.relaySelection
        )

        waitForSubscription?.let { delay(it) }

        val posts = listPosts(
            isPosts = feedSettings.isPosts,
            isReplies = feedSettings.isReplies,
            authorPubkeys = authorPubkeys,
            relays = feedSettings.relaySelection.getSelectedRelays(),
            until = until ?: getCurrentTimeInSeconds(),
            limit = limit,
        )

        return if (posts.isEmpty()) flow { emit(listOf()) }
        else postMapper.mapToPostsWithMetaFlow(posts)
    }

    private fun subscribeToFeed(
        authorPubkeys: List<String>?,
        isReplies: Boolean,
        limit: Int,
        until: Long?,
        relaySelection: RelaySelection
    ) {
        if (authorPubkeys != null && authorPubkeys.isEmpty()) return

        // We can't exclude replies in relay subscriptions,
        // so we increase the limit for post-only settings
        // to increase the chance of receiving more posts.
        val adjustedLimit = if (isReplies) 2 * limit else 3 * limit

        when (relaySelection) {
            is AllRelays, is MultipleRelays -> {
                nostrSubscriber.subscribeToFeed(
                    authorPubkeys = authorPubkeys,
                    limit = adjustedLimit,
                    until = until,
                    relays = relaySelection.getSelectedRelays()
                )
            }
            is UserSpecific -> {
                if (authorPubkeys == null) {
                    nostrSubscriber.subscribeToFeed(
                        authorPubkeys = null,
                        limit = adjustedLimit,
                        until = until,
                        relays = relaySelection.getSelectedRelays()
                    )
                } else {
                    relaySelection.pubkeysPerRelay.forEach { (relay, pubkeys) ->
                        // We ignore authorPubkeys because relaySelection should contain them
                        if (pubkeys.isNotEmpty()) {
                            nostrSubscriber.subscribeToFeed(
                                authorPubkeys = pubkeys,
                                limit = adjustedLimit,
                                until = until,
                                relays = listOf(relay)
                            )
                        }
                    }
                }

            }
        }

    }

    private fun listPubkeys(authorSelection: AuthorSelection): List<String>? {
        return when (authorSelection) {
            is Everyone -> null
            is Contacts -> contactListProvider.listPersonalContactPubkeys()
            is SingleAuthor -> listOf(authorSelection.pubkey)
        }
    }

    private suspend fun listPosts(
        isPosts: Boolean,
        isReplies: Boolean,
        authorPubkeys: List<String>?,
        relays: Collection<String>?,
        until: Long,
        limit: Int,
    ): List<PostEntity> {
        if (!isPosts && !isReplies) return listOf()

        return if (authorPubkeys == null && relays == null) {
            Log.d(TAG, "Get global feed")
            postDao.getGlobalFeed(
                isPosts = isPosts,
                isReplies = isReplies,
                until = until,
                limit = limit,
            )
        } else if (authorPubkeys == null && relays != null) {
            Log.d(TAG, "Get global feed by relays $relays")
            if (relays.isEmpty()) listOf()
            else postDao.getGlobalFeedByRelays(
                isPosts = isPosts,
                isReplies = isReplies,
                relays = relays,
                until = until,
                limit = limit,
            )
        } else if (authorPubkeys != null && relays == null) {
            Log.d(TAG, "Get authored feed")
            if (authorPubkeys.isEmpty()) listOf()
            else postDao.getAuthoredFeed(
                isPosts = isPosts,
                isReplies = isReplies,
                authorPubkeys = authorPubkeys,
                until = until,
                limit = limit,
            )
        } else if (authorPubkeys != null && relays != null) {
            Log.d(TAG, "Get authored feed by relays")
            if (authorPubkeys.isEmpty() || relays.isEmpty()) listOf()
            else postDao.getAuthoredFeedByRelays(
                isPosts = isPosts,
                isReplies = isReplies,
                authorPubkeys = authorPubkeys,
                relays = relays,
                until = until,
                limit = limit,
            )
        } else {
            Log.w(TAG, "Could not find correct db call. Default to empty list")
            listOf()
        }
    }
}
