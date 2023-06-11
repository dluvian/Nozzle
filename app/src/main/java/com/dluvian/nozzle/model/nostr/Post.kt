package com.dluvian.nozzle.model.nostr

data class Post(
    val replyTo: ReplyTo? = null,
    val mentions: List<String> = listOf(),
    val repostId: RepostId? = null,
    val msg: String,
)
