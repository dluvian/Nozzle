package com.dluvian.nozzle.ui.components.text

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dluvian.nozzle.ui.theme.hintGray

@Composable
fun Bullet(color: Color = MaterialTheme.colors.hintGray) {
    Text(
        text = "\u2022",
        color = color,
    )
}
