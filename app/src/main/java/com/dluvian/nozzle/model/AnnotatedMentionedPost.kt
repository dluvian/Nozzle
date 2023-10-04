package com.dluvian.nozzle.model

import androidx.compose.ui.text.AnnotatedString

data class AnnotatedMentionedPost(
    val annotatedContent: AnnotatedString,
    val mentionedPost: MentionedPost,
)
