package com.dluvian.nozzle.data.nostr

import com.dluvian.nozzle.model.PostWithMeta

interface INostrSubscriber {
    fun subscribeToProfileMetadataAndContactList(
        pubkeys: Collection<String>,
        relays: Collection<String>? = null
    ): List<String>

    fun subscribeToFeedPosts(
        authorPubkeys: Collection<String>?,
        limit: Int,
        until: Long?,
        relays: Collection<String>? = null,
    ): List<String>

    fun subscribeToReferencedData(
        posts: Collection<PostWithMeta>,
        relays: Collection<String>? = null
    ): List<String>

    fun subscribeThread(
        currentPostId: String,
        replyToId: String? = null,
        relays: Collection<String>? = null,
    ): List<String>

    fun subscribeProfiles(
        pubkeys: Collection<String>,
        relays: Collection<String>? = null
    ): List<String>

    fun subscribeNip65(pubkeys: Collection<String>): List<String>

    fun unsubscribeFeeds()

    fun unsubscribeReferencedPostsData()

    fun unsubscribeThread()

    fun unsubscribeProfiles()

    fun unsubscribeNip65()
}
