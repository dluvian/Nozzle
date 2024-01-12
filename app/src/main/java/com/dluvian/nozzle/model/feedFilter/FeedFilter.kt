package com.dluvian.nozzle.model.feedFilter


data class FeedFilter(
    val isPosts: Boolean,
    val isReplies: Boolean,
    val hashtag: String?,
    val authorFilter: AuthorFilter,
    val relayFilter: RelayFilter,
)
