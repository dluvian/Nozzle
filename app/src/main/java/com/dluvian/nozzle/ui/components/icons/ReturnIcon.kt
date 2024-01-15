package com.dluvian.nozzle.ui.components.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ReturnIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    description: String? = null,
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        tint = tint,
        contentDescription = description,
    )
}
