package com.dluvian.nozzle.data.nostr

import android.util.Log
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.nostr.Filter

private const val TAG = "NostrSubscriber"

class NostrSubscriber(private val nostrService: INostrService) : INostrSubscriber {
    override fun subscribeFullProfile(
        pubkey: String,
        relays: Collection<String>?
    ): List<String> {
        Log.i(TAG, "Subscribe full profile of $pubkey")
        val pubkeys = listOf(pubkey)
        val nip65Filter = Filter.createNip65Filter(pubkeys = pubkeys)
        val profileFilter = Filter.createProfileFilter(pubkeys = pubkeys)
        val contactListFilter = Filter.createContactListFilter(pubkeys = pubkeys)

        val mainSubIds = nostrService.subscribe(
            filters = listOf(profileFilter, contactListFilter),
            unsubOnEOSE = true,
            relays = relays
        )

        return mainSubIds + nostrService.subscribe(
            filters = listOf(nip65Filter),
            unsubOnEOSE = true,
            relays = null
        )
    }

    override fun subscribeFullProfiles(pubkeysByRelay: Map<Relay, List<Pubkey>>): List<String> {
        if (pubkeysByRelay.isEmpty()) return emptyList()

        val allSubIds = mutableListOf<String>()
        for (entry in pubkeysByRelay) {
            if (entry.value.isEmpty()) continue
            val nip65Filter = Filter.createNip65Filter(pubkeys = entry.value)
            val profileFilter = Filter.createProfileFilter(pubkeys = entry.value)
            val contactListFilter = Filter.createContactListFilter(pubkeys = entry.value)
            val allFilters = listOf(nip65Filter, profileFilter, contactListFilter)

            val subIds = nostrService.subscribe(
                filters = allFilters,
                unsubOnEOSE = true,
                relays = listOf(entry.key),
            )
            allSubIds.addAll(subIds)
        }

        return allSubIds
    }


    // TODO: Review the code later
    override fun subscribeSimpleProfiles(
        relaysByPubkey: Map<Pubkey, List<Relay>>,
        defaultRelays: Collection<Relay>
    ): Collection<String> {
        if (relaysByPubkey.isEmpty()) return emptyList()

        val allSubIds = mutableListOf<String>()

        val pubkeysByRelays = mutableMapOf<Relay, MutableList<Pubkey>>()
        for ((pubkey, relays) in relaysByPubkey) {
            relays.ifEmpty { defaultRelays }.forEach { relay ->
                val present = pubkeysByRelays.putIfAbsent(relay, mutableListOf(pubkey))
                present?.add(pubkey)
            }
        }
        pubkeysByRelays.forEach { (relay, pubkeys) ->
            val profileFilter = Filter.createProfileFilter(pubkeys = pubkeys)
            val subIds = nostrService.subscribe(
                filters = listOf(profileFilter),
                unsubOnEOSE = true,
                relays = listOf(relay),
            )
            allSubIds.addAll(subIds)
        }

        return allSubIds
    }

    override fun subscribeToFeedPosts(
        authorPubkeys: List<String>?,
        hashtag: String?,
        limit: Int,
        until: Long,
        relays: Collection<String>?
    ): List<String> {
        Log.i(TAG, "Subscribe to feed of ${authorPubkeys?.size} pubkeys in ${relays?.size} relays")
        val postFilter = Filter.createPostFilter(
            pubkeys = authorPubkeys,
            t = hashtag?.let { listOf(hashtag, hashtag.lowercase()).distinct() },
            until = until,
            limit = limit
        )

        return nostrService.subscribe(
            filters = listOf(postFilter),
            unsubOnEOSE = true,
            relays = relays,
        )
    }

    override fun subscribeFeedInfo(
        nip65PubkeysByRelay: Map<Relay, List<Pubkey>>,
        profilePubkeysByRelay: Map<Relay, List<Pubkey>>,
        contactListPubkeysByRelay: Map<Relay, List<Pubkey>>,
        postIdsByRelay: Map<Relay, List<Pubkey>>,
        repliesByRelay: Map<Relay, List<String>>,
        reactionPostIdsByRelay: Map<Relay, List<String>>,
        reactorPubkey: String
    ): List<String> {
        val allRelays = nip65PubkeysByRelay.keys +
                profilePubkeysByRelay.keys +
                contactListPubkeysByRelay.keys +
                postIdsByRelay.keys +
                repliesByRelay.keys +
                reactionPostIdsByRelay.keys
        if (allRelays.isEmpty()) return emptyList()

        val allSubIds = mutableListOf<String>()
        for (relay in allRelays) {
            val allFilters = createFeedInfoFilters(
                nip65Pubkeys = nip65PubkeysByRelay[relay],
                profilePubkeys = profilePubkeysByRelay[relay],
                contactListPubkeys = contactListPubkeysByRelay[relay],
                postIds = postIdsByRelay[relay],
                replies = repliesByRelay[relay],
                reactionPostIds = reactionPostIdsByRelay[relay],
                reactorPubkey = reactorPubkey
            )
            if (allFilters.isEmpty()) continue

            val ids = nostrService.subscribe(
                filters = allFilters,
                unsubOnEOSE = true,
                relays = listOf(relay),
            )
            allSubIds.addAll(ids)
        }

        return allSubIds
    }

    override fun subscribeNip65AndProfiles(
        nip65PubkeysByRelay: Map<Relay, List<Pubkey>>,
        pubkeysByRelay: Map<Relay, List<Pubkey>>
    ): List<String> {
        val subIds = mutableListOf<String>()
        if (nip65PubkeysByRelay.isNotEmpty()) {
            nip65PubkeysByRelay.forEach {
                if (it.value.isNotEmpty()) {
                    val ids = nostrService.subscribe(
                        filters = listOf(Filter.createNip65Filter(it.value)),
                        unsubOnEOSE = true,
                        relays = listOf(it.key),
                    )
                    subIds.addAll(ids)
                }
            }
        }
        if (pubkeysByRelay.isNotEmpty()) {
            pubkeysByRelay.forEach {
                if (it.value.isNotEmpty()) {
                    val ids = nostrService.subscribe(
                        filters = listOf(Filter.createProfileFilter(it.value)),
                        unsubOnEOSE = true,
                        relays = listOf(it.key),
                    )
                    subIds.addAll(ids)
                }
            }
        }

        return subIds
    }

    private fun createFeedInfoFilters(
        nip65Pubkeys: List<String>?,
        profilePubkeys: List<String>?,
        contactListPubkeys: List<String>?,
        postIds: List<String>?,
        replies: List<String>?,
        reactionPostIds: List<String>?,
        reactorPubkey: String
    ): List<Filter> {
        val allFilters = mutableListOf<Filter>()
        nip65Pubkeys?.let {
            if (it.isNotEmpty()) allFilters.add(Filter.createNip65Filter(pubkeys = it))
        }
        profilePubkeys?.let {
            if (it.isNotEmpty()) allFilters.add(Filter.createProfileFilter(pubkeys = it))

        }
        contactListPubkeys?.let {
            if (it.isNotEmpty()) allFilters.add(Filter.createContactListFilter(pubkeys = it))

        }
        postIds?.let {
            if (it.isNotEmpty()) allFilters.add(Filter.createPostFilter(ids = it))

        }
        replies?.let {
            if (it.isNotEmpty()) allFilters.add(Filter.createPostFilter(e = it))

        }
        reactionPostIds?.let {
            if (it.isNotEmpty()) allFilters.add(
                Filter.createReactionFilter(
                    e = it,
                    pubkeys = listOf(reactorPubkey)
                )
            )
        }

        return allFilters
    }

    override fun subscribePosts(
        postIds: List<String>,
        relays: Collection<String>?
    ): List<String> {
        Log.i(TAG, "Subscribe to ${postIds.size} posts in ${relays?.size} relays")
        val postFilter = Filter.createPostFilter(
            ids = postIds,
            until = getCurrentTimeInSeconds(),
        )

        return nostrService.subscribe(
            filters = listOf(postFilter),
            unsubOnEOSE = true,
            relays = relays,
        )
    }

    override fun subscribePostsWithMention(
        mentionedPubkey: String,
        limit: Int,
        until: Long,
        relays: Collection<String>?
    ): List<String> {
        val mentionFilter = Filter.createPostFilter(
            p = listOf(mentionedPubkey),
            limit = limit,
            until = until,
        )

        return nostrService.subscribe(
            filters = listOf(mentionFilter),
            unsubOnEOSE = true,
            relays = relays,
        )
    }

    // No relaySelection needed because nip65 could be anywhere
    override fun subscribeNip65(pubkeys: List<String>): List<String> {
        if (pubkeys.isEmpty()) return emptyList()

        Log.i(TAG, "Subscribe to ${pubkeys.size} nip65s")
        val nip65Filter = Filter.createNip65Filter(pubkeys = pubkeys)

        return nostrService.subscribe(
            filters = listOf(nip65Filter),
            unsubOnEOSE = true,
            relays = null
        )
    }

    override fun unsubscribe(subscriptionIds: Collection<String>) {
        nostrService.unsubscribe(subscriptionIds)
    }
}
