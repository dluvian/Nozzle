package com.dluvian.nozzle.ui.components.indicators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dluvian.nozzle.data.BAD_PING_THRESHOLD
import com.dluvian.nozzle.data.GOOD_PING_THRESHOLD
import com.dluvian.nozzle.model.Offline
import com.dluvian.nozzle.model.Online
import com.dluvian.nozzle.model.OnlineStatus
import com.dluvian.nozzle.model.Waiting
import com.dluvian.nozzle.ui.theme.HintGray
import com.dluvian.nozzle.ui.theme.sizing

@Composable
fun OnlineStatusIndicator(onlineStatus: OnlineStatus) {
    if (onlineStatus is Waiting) CircularProgressIndicator(
        modifier = Modifier
            .size(sizing.tinyItem)
            .aspectRatio(1f),
        strokeWidth = 1.dp
    ) else OnlineStatusDot(onlineStatus = onlineStatus)
}

@Composable
private fun OnlineStatusDot(onlineStatus: OnlineStatus) {
    val color = when (onlineStatus) {
        is Online -> if (onlineStatus.ping < GOOD_PING_THRESHOLD) Color.Green
        else if (onlineStatus.ping < BAD_PING_THRESHOLD) Color.Yellow
        else Color.Red

        is Waiting, is Offline -> HintGray
    }
    Box(
        modifier = Modifier
            .size(sizing.tinyItem)
            .background(color = color, shape = CircleShape)
    )
}
