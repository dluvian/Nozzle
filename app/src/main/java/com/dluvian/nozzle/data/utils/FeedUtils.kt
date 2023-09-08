package com.dluvian.nozzle.data.utils

import com.dluvian.nozzle.model.PostWithMeta

fun listReferencedPostIds(posts: Collection<PostWithMeta>): List<String> {
    if (posts.isEmpty()) return emptyList()

    val referencedPostIds = mutableListOf<String>()
    for (post in posts) {
        post.entity.replyToId?.let { referencedPostIds.add(it) }
    }

    return referencedPostIds.distinct()
}

fun getIdsPerRelayHintMap(posts: Collection<PostWithMeta>): Map<String, List<String>> {
    if (posts.isEmpty()) return emptyMap()

    val result = mutableMapOf<String, MutableList<String>>()

    posts.forEach { post ->
        if (post.entity.replyRelayHint != null && post.entity.replyToId != null) {
            val current =
                result.putIfAbsent(post.entity.replyRelayHint, mutableListOf(post.entity.replyToId))
            if (current != null) result[post.entity.replyRelayHint]?.add(post.entity.replyToId)
        }
    }

    return result
}

fun hasUnknownParentAuthor(post: PostWithMeta): Boolean {
    return post.entity.replyToId != null
            && (post.replyToPubkey.isNullOrEmpty() || post.replyToName.isNullOrEmpty())
}
