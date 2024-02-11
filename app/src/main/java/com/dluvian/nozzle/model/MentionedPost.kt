package com.dluvian.nozzle.model

data class MentionedPost(
    val id: NoteId,
    val pubkey: Pubkey?,
    val content: String?,
    val name: String?,
    val picture: String?,
    val createdAt: Long?,
)
