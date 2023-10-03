package com.dluvian.nozzle.model.nostr

data class Post(
    val content: String,
    val mentions: List<String>
)
