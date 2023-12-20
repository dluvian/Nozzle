package com.dluvian.nozzle.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

val MentionAndHashtagStyle = SpanStyle(color = HyperlinkBlue)

val HyperlinkStyle = MentionAndHashtagStyle.copy(textDecoration = TextDecoration.Underline)

val BoldStyle = SpanStyle(fontWeight = FontWeight.Bold)

val HintGrayStyle: SpanStyle
    @Composable
    get() = SpanStyle(color = MaterialTheme.colors.hintGray)

val BoldHintGrayStyle: SpanStyle
    @Composable
    get() = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.hintGray)
