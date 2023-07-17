package com.dluvian.nozzle.model

// TODO: Remove rootId?
data class PostIds(
    val id: String,
    val replyToId: String?,
    val replyToRootId: String?,
)
