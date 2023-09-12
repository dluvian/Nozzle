package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.utils.IdExtractorUtils.extractNeventsAndNoteIds
import com.dluvian.nozzle.data.nostr.utils.IdExtractorUtils.extractNprofilesAndNpubs
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.helper.BasePost
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

private const val TAG = "FeedProvider"

class FeedProvider(
    private val postWithMetaProvider: IPostWithMetaProvider,
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
        nostrSubscriber.unsubscribeReferencedPostsData()
        val authorSelectionPubkeys = listPubkeys(authorSelection = feedSettings.authorSelection)
        subscribeToFeedPosts(
            isReplies = feedSettings.isReplies,
            limit = limit,
            until = until,
            authorPubkeys = authorSelectionPubkeys,
            relaySelection = feedSettings.relaySelection
        )

        // TODO: Use channel
        waitForSubscription?.let { delay(it) }

        val basePosts = listBasePosts(
            isPosts = feedSettings.isPosts,
            isReplies = feedSettings.isReplies,
            authorPubkeys = authorSelectionPubkeys,
            relays = feedSettings.relaySelection.getSelectedRelays(),
            until = until ?: getCurrentTimeInSeconds(),
            limit = limit,
        )

        // TODO: Don't resub all the time
        val foundAuthorPubkeys = basePosts.map { it.pubkey }.distinct()
        nostrSubscriber.subscribeProfiles(
            pubkeys = foundAuthorPubkeys,
            relays = feedSettings.relaySelection.getSelectedRelays()
        )
        // TODO: Subscribe replies in read relays

        val contents = basePosts.map { it.content }
        val mentionedNprofiles = extractNprofilesAndNpubs(contents = contents)
        mentionedNprofiles.forEach {
            nostrSubscriber.subscribeProfile(
                pubkey = it.pubkey,
                relays = it.relays.ifEmpty { feedSettings.relaySelection.getSelectedRelays() }
            )
        }
        extractNeventsAndNoteIds(contents = contents).forEach {
            nostrSubscriber.subscribePost(
                postId = it.eventId,
                relays = it.relays.ifEmpty { feedSettings.relaySelection.getSelectedRelays() })
        }

        return postWithMetaProvider.getPostsWithMetaFlow(
            postIds = basePosts.map { it.id }.distinct(),
            authorPubkeys = foundAuthorPubkeys,
            mentionedPubkeys = mentionedNprofiles.map { it.pubkey }
        )
    }

    private fun subscribeToFeedPosts(
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
                nostrSubscriber.subscribeToFeedPosts(
                    authorPubkeys = authorPubkeys,
                    limit = adjustedLimit,
                    until = until,
                    relays = relaySelection.getSelectedRelays()
                )
            }

            is UserSpecific -> {
                if (authorPubkeys == null) {
                    nostrSubscriber.subscribeToFeedPosts(
                        authorPubkeys = null,
                        limit = adjustedLimit,
                        until = until,
                        relays = relaySelection.getSelectedRelays()
                    )
                } else {
                    relaySelection.pubkeysPerRelay.forEach { (relay, pubkeys) ->
                        // We ignore authorPubkeys because relaySelection should contain them
                        if (pubkeys.isNotEmpty()) {
                            nostrSubscriber.subscribeToFeedPosts(
                                authorPubkeys = pubkeys.toList(),
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

    private suspend fun listPubkeys(authorSelection: AuthorSelection): List<String>? {
        return when (authorSelection) {
            is Everyone -> null
            is Contacts -> contactListProvider.listPersonalContactPubkeys()
            is SingleAuthor -> listOf(authorSelection.pubkey)
        }
    }

    private suspend fun listBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        authorPubkeys: List<String>?,
        relays: Collection<String>?,
        until: Long,
        limit: Int,
    ): List<BasePost> {
        if (!isPosts && !isReplies) return emptyList()

        return if (authorPubkeys == null && relays == null) {
            Log.d(TAG, "Get global feed")
            postDao.getGlobalFeedBasePosts(
                isPosts = isPosts,
                isReplies = isReplies,
                until = until,
                limit = limit,
            )
        } else if (authorPubkeys == null && relays != null) {
            Log.d(TAG, "Get global feed by ${relays.size} relays $relays")
            if (relays.isEmpty()) emptyList()
            else postDao.getGlobalFeedBasePostsByRelays(
                isPosts = isPosts,
                isReplies = isReplies,
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
