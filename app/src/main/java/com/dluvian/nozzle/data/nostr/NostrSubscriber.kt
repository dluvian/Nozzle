package com.dluvian.nozzle.data.nostr

import android.util.Log
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.data.utils.getIdsPerRelayHintMap
import com.dluvian.nozzle.data.utils.listReferencedPostIds
import com.dluvian.nozzle.data.utils.listReferencedPubkeys
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.nostr.Filter
import java.util.Collections

private const val TAG = "NostrSubscriber"

class NostrSubscriber(
    private val nostrService: INostrService,
    private val pubkeyProvider: IPubkeyProvider,
    private val postDao: PostDao
) : INostrSubscriber {
    private val feedSubscriptions = Collections.synchronizedList(mutableListOf<String>())
    private val threadSubscriptions = Collections.synchronizedList(mutableListOf<String>())
    private val additionalFeedDataSubscriptions =
        Collections.synchronizedList(mutableListOf<String>())
    private val profileSubscriptions = Collections.synchronizedList(mutableListOf<String>())
    private val nip65Subscriptions = Collections.synchronizedList(mutableListOf<String>())

    override fun subscribeToProfileMetadataAndContactList(
        pubkeys: Collection<String>,
        relays: Collection<String>?
    ): List<String> {
        Log.i(TAG, "Subscribe metadata and contact list for ${pubkeys.size} pubkeys")
        val profileFilter = Filter.createProfileFilter(pubkeys = pubkeys.toList())
        val contactListFilter = Filter.createContactListFilter(pubkeys = pubkeys.toList())

        val ids = nostrService.subscribe(
            filters = listOf(profileFilter, contactListFilter),
            unsubOnEOSE = true,
            relays = relays
        )
        profileSubscriptions.addAll(ids)

        return ids
    }

    override fun subscribeToFeed(
        authorPubkeys: Collection<String>?,
        limit: Int,
        until: Long?,
        relays: Collection<String>?
    ): List<String> {
        Log.d(TAG, "Subscribe to feed of ${authorPubkeys?.size} pubkeys in ${relays?.size} relays")
        val postFilter = Filter.createPostFilter(
            pubkeys = authorPubkeys,
            until = until ?: getCurrentTimeInSeconds(),
            limit = limit
        )
        val ids = nostrService.subscribe(
            filters = listOf(postFilter),
            unsubOnEOSE = true,
            relays = relays,
        )
        feedSubscriptions.addAll(ids)

        return ids
    }

    override suspend fun subscribeToAdditionalPostsData(
        posts: Collection<PostWithMeta>,
        relays: Collection<String>?,
    ): List<String> {
        Log.i(TAG, "Subscribe to additional posts data")
        if (posts.isEmpty()) return emptyList()

        // TODO: First referenced posts, then referenced authors.
        // At the same time does not make sense bc we don't know which pubkeys to sub yet
        // TODO: Show shortened npub after referenced post is found

        val postIds = posts.map { it.id }
        val referencedPostIds = listReferencedPostIds(posts)
        val referencedPubkeys = mutableSetOf<String>()
        referencedPubkeys.addAll(listReferencedPubkeys(posts))

        val filters = mutableListOf<Filter>()
        filters.add(
            Filter.createReactionFilter(
                e = postIds,
                pubkeys = listOf(pubkeyProvider.getPubkey())
            )
        )
        filters.add(Filter.createPostFilter(e = postIds))
        if (referencedPostIds.isNotEmpty()) {
            referencedPubkeys.addAll(postDao.listAuthorPubkeys(referencedPostIds))
            filters.add(Filter.createPostFilter(ids = referencedPostIds))
        }
        if (referencedPubkeys.isNotEmpty()) {
            filters.add(Filter.createProfileFilter(pubkeys = referencedPubkeys.toList()))
        }

        val followedAuthorPubkeys = posts.filter { it.isFollowedByMe }.map { it.pubkey }.distinct()
        if (followedAuthorPubkeys.isNotEmpty()) {
            filters.add(Filter.createContactListFilter(pubkeys = followedAuthorPubkeys))
        }

        val ids = nostrService.subscribe(
            filters = filters,
            unsubOnEOSE = true,
            relays = relays,
        ).toMutableList()
        ids.addAll(subscribeReplyRelayHint(posts, relays))
        additionalFeedDataSubscriptions.addAll(ids)

        return ids
    }

    private fun subscribeReplyRelayHint(
        posts: Collection<PostWithMeta>,
        relays: Collection<String>?,
    ): List<String> {
        val ids = mutableListOf<String>()
        getIdsPerRelayHintMap(posts = posts).forEach { (relayHint, hintedPostIds) ->
            // TODO: Relay URL utils for checking this
            if (relays?.contains(relayHint) == false
                && relayHint.startsWith("wss://")
                && hintedPostIds.isNotEmpty()
            ) {
                Log.i(
                    TAG,
                    "Subscribe to reply relay hint of ${hintedPostIds.size} posts in $relayHint"
                )
                val relayHintIds = nostrService.subscribe(
                    filters = listOf(Filter.createPostFilter(ids = hintedPostIds)),
                    unsubOnEOSE = true,
                    relays = listOf(relayHint),
                )
                ids.addAll(relayHintIds)
            }
        }

        return ids
    }

    override fun subscribeThread(
        currentPostId: String,
        replyToId: String?,
        relays: Collection<String>?,
    ): List<String> {
        Log.i(TAG, "Subscribe to thread")

        val postIds = mutableListOf(currentPostId)
        replyToId?.let { postIds.add(it) }

        val filters = mutableListOf<Filter>()
        filters.add(Filter.createPostFilter(e = postIds))
        filters.add(Filter.createPostFilter(ids = postIds))

        val ids = nostrService.subscribe(
            filters = filters,
            unsubOnEOSE = true,
            relays = relays,
        )
        threadSubscriptions.addAll(ids)

        return ids
    }

    override fun subscribeProfiles(
        pubkeys: Collection<String>,
        relays: Collection<String>?,
    ): List<String> {
        Log.i(TAG, "Subscribe to ${pubkeys.size} profiles")

        if (pubkeys.isEmpty()) return emptyList()

        val profileFilter = Filter.createProfileFilter(pubkeys = pubkeys.toList())

        val ids = nostrService.subscribe(
            filters = listOf(profileFilter),
            unsubOnEOSE = true,
            relays = relays
        )
        profileSubscriptions.addAll(ids)

        return ids
    }

    // No relaySelection needed because nip65 could be anywhere
    override fun subscribeNip65(pubkeys: Collection<String>): List<String> {
        Log.i(TAG, "Subscribe to ${pubkeys.size} nip65s")

        if (pubkeys.isEmpty()) return emptyList()

        val nip65Filter = Filter.createNip65Filter(pubkeys = pubkeys.toList())

        val ids = nostrService.subscribe(
            filters = listOf(nip65Filter),
            unsubOnEOSE = true,
            relays = null
        )
        nip65Subscriptions.addAll(ids)

        return ids
    }

    override fun unsubscribeFeeds() {
        val snapshot = feedSubscriptions.toList()
        nostrService.unsubscribe(snapshot)
        feedSubscriptions.removeAll(snapshot)
    }

    override fun unsubscribeAdditionalPostsData() {
        val snapshot = additionalFeedDataSubscriptions.toList()
        nostrService.unsubscribe(snapshot)
        additionalFeedDataSubscriptions.removeAll(snapshot)
    }

    override fun unsubscribeThread() {
        val snapshot = threadSubscriptions.toList()
        nostrService.unsubscribe(snapshot)
        threadSubscriptions.removeAll(snapshot)
    }

    override fun unsubscribeProfiles() {
        val snapshot = profileSubscriptions.toList()
        nostrService.unsubscribe(snapshot)
        profileSubscriptions.removeAll(snapshot)
    }

    override fun unsubscribeNip65() {
        val snapshot = nip65Subscriptions.toList()
        nostrService.unsubscribe(snapshot)
        nip65Subscriptions.removeAll(snapshot)
    }
}
