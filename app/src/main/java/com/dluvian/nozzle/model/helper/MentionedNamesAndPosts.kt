package com.dluvian.nozzle.model.helper

import com.dluvian.nozzle.model.MentionedPost

data class MentionedNamesAndPosts(
    val mentionedNamesByPubkey: Map<Pubkey, String>,
    val mentionedPostsById: Map<String, MentionedPost>
)
