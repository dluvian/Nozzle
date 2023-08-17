package com.dluvian.nozzle.data.utils

import com.dluvian.nozzle.model.PostWithMeta

fun listReferencedPostIds(posts: Collection<PostWithMeta>): List<String> {
    if (posts.isEmpty()) return emptyList()

    val referencedPostIds = mutableListOf<String>()
    for (post in posts) {
        post.replyToId?.let { referencedPostIds.add(it) }
        post.mentionedPost?.id?.let { referencedPostIds.add(it) }
    }

    return referencedPostIds.distinct()
}

fun getIdsPerRelayHintMap(posts: Collection<PostWithMeta>): Map<String, List<String>> {
    if (posts.isEmpty()) return emptyMap()

    val result = mutableMapOf<String, MutableList<String>>()

    posts.forEach { post ->
        if (post.replyRelayHint != null && post.replyToId != null) {
            val current = result.putIfAbsent(post.replyRelayHint, mutableListOf(post.replyToId))
            if (current != null) result[post.replyRelayHint]?.add(post.replyToId)
        }
    }

    return result
}

fun hasUnknownParentAuthor(post: PostWithMeta): Boolean {
    return post.replyToId != null
            && (post.replyToPubkey.isNullOrEmpty() || post.replyToName.isNullOrEmpty())
}

fun hasUnknownMentionedPostAuthor(post: PostWithMeta): Boolean {
    return post.mentionedPost?.let { it.pubkey.isEmpty() || it.name.isEmpty() } ?: false
}

fun hasUnknownReferencedAuthors(post: PostWithMeta): Boolean {
    return hasUnknownMentionedPostAuthor(post) || hasUnknownParentAuthor(post)
}
