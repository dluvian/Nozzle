package com.dluvian.nozzle.ui.theme

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration

val MentionAndHashtagStyle = SpanStyle(color = HyperlinkBlue)

val HyperlinkStyle = MentionAndHashtagStyle.copy(textDecoration = TextDecoration.Underline)
