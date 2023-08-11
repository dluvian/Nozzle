package com.dluvian.nozzle.ui.components.text

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Bullet(color: Color = Color.LightGray) {
    Text(
        text = "\u2022",
        color = color,
    )
}
