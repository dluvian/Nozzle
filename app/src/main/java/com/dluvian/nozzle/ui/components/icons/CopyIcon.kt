package com.dluvian.nozzle.ui.components.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CopyIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    description: String? = null,
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.ContentCopy,
        contentDescription = description,
        tint = tint,
    )
}