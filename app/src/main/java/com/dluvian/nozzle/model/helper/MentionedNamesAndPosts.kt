package com.dluvian.nozzle.model.helper

import com.dluvian.nozzle.model.MentionedPost

data class MentionedNamesAndPosts(
    val mentionedPubkeyToNameMap: Map<String, String>,
    val mentionedPostIdToPostMap: Map<String, MentionedPost>
)
