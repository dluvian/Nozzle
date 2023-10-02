package com.dluvian.nozzle.model


data class FeedSettings(
    val isPosts: Boolean,
    val isReplies: Boolean,
    val hashtag: String?,
    val authorSelection: AuthorSelection,
    val relaySelection: RelaySelection,
)
