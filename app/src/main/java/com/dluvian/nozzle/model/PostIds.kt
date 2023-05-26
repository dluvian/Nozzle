package com.dluvian.nozzle.model

data class PostIds(
    val id: String,
    val replyToId: String?,
    val replyToRootId: String?,
)
