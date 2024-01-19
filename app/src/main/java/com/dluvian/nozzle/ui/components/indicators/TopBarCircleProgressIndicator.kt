package com.dluvian.nozzle.ui.components.indicators

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.sizing

@Composable
fun TopBarCircleProgressIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(sizing.mediumItem)
            .aspectRatio(1f),
        strokeWidth = sizing.smallProgressIndicatorStroke
    )
}
