package com.dluvian.nozzle.model

data class PostWithTagsAndMentions(
    val content: String,
    val mentions: List<String>,
    val hashtags: List<String>,
)
