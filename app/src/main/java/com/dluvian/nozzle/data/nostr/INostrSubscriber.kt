package com.dluvian.nozzle.data.nostr

import com.dluvian.nozzle.model.PostWithMeta

interface INostrSubscriber {
    fun subscribeToProfileAndContactList(
        pubkeys: Collection<String>,
        relays: Collection<String>? = null
    ): List<String>

    fun subscribeToFeedPosts(
        authorPubkeys: List<String>?,
        limit: Int,
        until: Long?,
        relays: Collection<String>? = null,
    ): List<String>


    fun subscribePost(postId: String, relays: Collection<String>? = null) =
        subscribePosts(postIds = listOf(postId), relays = relays)

    fun subscribePosts(
        postIds: List<String>,
        relays: Collection<String>? = null
    ): List<String>

    fun subscribeToReferencedData(
        posts: Collection<PostWithMeta>,
        relays: Collection<String>? = null
    ): List<String>

    fun subscribeThread(
        currentPostId: String,
        relays: Collection<String>? = null,
    ): List<String>

    fun subscribeProfile(pubkey: String, relays: Collection<String>? = null) =
        subscribeProfiles(pubkeys = listOf(pubkey), relays = relays)

    fun subscribeProfiles(
        pubkeys: Collection<String>,
        relays: Collection<String>? = null
    ): List<String>

    fun subscribeNip65(pubkeys: Collection<String>): List<String>

    fun unsubscribeFeeds()

    fun unsubscribeReferencedPostsData()

    fun unsubscribeThread()

    fun unsubscribeProfileMetadataAndContactLists()

    fun unsubscribeNip65()
}
