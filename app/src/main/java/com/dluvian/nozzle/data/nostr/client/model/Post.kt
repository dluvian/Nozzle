package com.dluvian.nozzle.data.nostr.client.model

data class Post(
    val replyTo: ReplyTo? = null,
    val mentions: List<String> = listOf(),
    val repostId: RepostId? = null,
    val msg: String,
)
