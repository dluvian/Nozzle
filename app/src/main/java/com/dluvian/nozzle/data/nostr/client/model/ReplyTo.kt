package com.dluvian.nozzle.data.nostr.client.model

data class ReplyTo(
    val replyToRoot: String?,
    val replyTo: String,
    val relayUrl: String
)
