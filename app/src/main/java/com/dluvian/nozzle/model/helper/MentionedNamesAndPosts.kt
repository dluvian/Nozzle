package com.dluvian.nozzle.model.helper

import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.Pubkey

data class MentionedNamesAndPosts(
    val mentionedNamesByPubkey: Map<Pubkey, String>,
    val mentionedPostsById: Map<String, MentionedPost>
)
