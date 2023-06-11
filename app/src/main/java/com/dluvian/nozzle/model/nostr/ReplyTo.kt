package com.dluvian.nozzle.model.nostr

data class ReplyTo(
    val replyToRoot: String?,
    val replyTo: String,
    val relayUrl: String
)
