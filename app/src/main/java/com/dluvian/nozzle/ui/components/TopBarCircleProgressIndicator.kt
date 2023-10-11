package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dluvian.nozzle.ui.theme.sizing

@Composable
fun TopBarCircleProgressIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .aspectRatio(1f),
        color = Color.White,
        strokeWidth = sizing.smallProgressIndicator
    )
}
