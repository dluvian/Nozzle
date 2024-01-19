package com.dluvian.nozzle.ui.components.text

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun NamedItem(
    item: @Composable () -> Unit,
    name: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        item()
        Text(text = name, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
