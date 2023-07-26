package com.dluvian.nozzle.model.nostr

data class Post(
    val replyTo: ReplyTo? = null,
    val mentions: List<String> = emptyList(),
    val msg: String,
)
