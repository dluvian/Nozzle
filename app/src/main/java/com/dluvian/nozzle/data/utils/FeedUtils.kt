package com.dluvian.nozzle.data.utils

import com.dluvian.nozzle.model.PostWithMeta

fun listReferencedPubkeys(posts: Collection<PostWithMeta>): List<String> {
    if (posts.isEmpty()) return emptyList()

    val referencedPubkeys = mutableListOf<String>()
    for (post in posts) {
        referencedPubkeys.add(post.pubkey)
        post.replyToPubkey?.let { referencedPubkeys.add(it) }
        post.repost?.pubkey?.let { referencedPubkeys.add(it) }
    }

    return referencedPubkeys.distinct()
}

fun listReferencedPostIds(posts: Collection<PostWithMeta>): List<String> {
    if (posts.isEmpty()) return emptyList()

    val referencedPostIds = mutableListOf<String>()
    for (post in posts) {
        post.replyToId?.let { referencedPostIds.add(it) }
        post.repost?.id?.let { referencedPostIds.add(it) }
    }

    return referencedPostIds.distinct()
}

fun getIdsPerRelayHintMap(posts: Collection<PostWithMeta>): Map<String, List<String>> {
    val result = mutableMapOf<String, MutableList<String>>()

    posts.forEach { post ->
        if (post.replyRelayHint != null && post.replyToId != null) {
            val current = result.putIfAbsent(post.replyRelayHint, mutableListOf(post.replyToId))
            if (current != null) result[post.replyRelayHint]?.add(post.replyToId)
        }
    }

    return result
}
