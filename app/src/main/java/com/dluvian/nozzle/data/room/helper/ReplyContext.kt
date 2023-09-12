package com.dluvian.nozzle.data.room.helper

data class ReplyContext(
    val id: String,
    val replyToId: String?,
    val pubkey: String,
    val content: String = ""
)
