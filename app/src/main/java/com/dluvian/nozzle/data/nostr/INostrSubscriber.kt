package com.dluvian.nozzle.data.nostr

import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay

interface INostrSubscriber {
    fun subscribeFullProfile(pubkey: String, relays: Collection<String>? = null): List<String>

    fun subscribeFullProfiles(pubkeysByRelay: Map<Relay, List<Pubkey>>): List<String>

    fun subscribeSimpleProfiles(
        relaysByPubkey: Map<Pubkey, List<Relay>>,
        defaultRelays: Collection<Relay>,
    ): Collection<String>

    fun subscribeToFeedPosts(
        authorPubkeys: List<String>?,
        hashtag: String?,
        limit: Int,
        until: Long = getCurrentTimeInSeconds(),
        relays: Collection<String>? = null,
    ): List<String>

    fun subscribeFeedInfo(
        nip65PubkeysByRelay: Map<Relay, List<Pubkey>>,
        profilePubkeysByRelay: Map<Relay, List<Pubkey>>,
        contactListPubkeysByRelay: Map<Relay, List<Pubkey>>,
        postIdsByRelay: Map<Relay, List<String>>,
        repliesByRelay: Map<Relay, List<String>>,
        reactionPostIdsByRelay: Map<Relay, List<String>>,
        reactorPubkey: String, // TODO: Move this to Map above
    ): List<String>

    fun subscribeNip65AndProfiles(
        nip65PubkeysByRelay: Map<Relay, List<Pubkey>>,
        pubkeysByRelay: Map<Relay, List<Pubkey>>,
    ): List<String>

    fun subscribePosts(
        postIds: List<String>,
        relays: Collection<String>? = null
    ): List<String>

    fun subscribePostsWithMention(
        mentionedPubkey: String,
        limit: Int,
        until: Long = getCurrentTimeInSeconds(),
        relays: Collection<String>? = null,
    ): List<String>

    fun subscribeLikes(
        pubkey: Pubkey,
        limit: Int,
        until: Long = getCurrentTimeInSeconds(),
        relays: Collection<String>? = null,
    ): List<String>

    fun subscribeNip65(pubkeys: List<String>): List<String>

    fun unsubscribe(subscriptionIds: Collection<String>)
}
