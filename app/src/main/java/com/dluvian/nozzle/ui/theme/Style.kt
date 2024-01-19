package com.dluvian.nozzle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

val MentionAndHashtagStyle = SpanStyle(color = HyperlinkBlue)

val HyperlinkStyle = MentionAndHashtagStyle.copy(textDecoration = TextDecoration.Underline)

val BoldStyle = SpanStyle(fontWeight = FontWeight.Bold)

val HintGrayStyle: SpanStyle
    @Composable
    get() = MaterialTheme.typography.bodyMedium.copy(color = HintGray).toSpanStyle()

val BoldHintGrayStyle: SpanStyle
    @Composable
    get() = HintGrayStyle.copy(fontWeight = FontWeight.Bold)
