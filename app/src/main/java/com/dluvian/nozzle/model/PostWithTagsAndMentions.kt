package com.dluvian.nozzle.model

data class PostWithTagsAndMentions(
    val content: String,
    val mentions: List<Pubkey>,
    val hashtags: List<String>,
    val quotes: List<NoteId>,
)
