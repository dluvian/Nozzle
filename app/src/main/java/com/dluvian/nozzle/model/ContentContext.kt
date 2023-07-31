package com.dluvian.nozzle.model

data class ContentContext(
    val cleanContent: String,
    val mentionedPostId: String? = null,
)
