package com.dluvian.nozzle.model

data class MentionedPost(
    val id: String,
    val pubkey: String,
    val content: String,
    val name: String?,
    val createdAt: Long,
)
