package com.dluvian.nozzle.model

data class MentionedNamesAndPosts(
    val mentionedNamesByPubkey: Map<Pubkey, String>,
    val mentionedPostsById: Map<String, MentionedPost>
)
