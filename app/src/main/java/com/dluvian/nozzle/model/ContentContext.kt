package com.dluvian.nozzle.model

data class ContentContext(
    val cleanContent: String,
    val mentionedPostIds: List<String> = emptyList(),
    val mediaUrls: List<String> = emptyList(),
)
