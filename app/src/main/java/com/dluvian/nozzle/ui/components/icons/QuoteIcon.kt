package com.dluvian.nozzle.ui.components.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun QuoteIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    description: String? = null,
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.FormatQuote,
        tint = tint,
        contentDescription = description,
    )
}