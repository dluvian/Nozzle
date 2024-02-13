package com.dluvian.nozzle.model

data class MentionedNamesAndPosts(
    val mentionedNamesByPubkey: Map<Pubkey, String> = emptyMap(),
    val mentionedPostsById: Map<String, MentionedPost> = emptyMap()
)
