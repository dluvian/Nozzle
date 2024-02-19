package com.dluvian.nozzle.ui.components.indicators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.BAD_PING_THRESHOLD
import com.dluvian.nozzle.data.GOOD_PING_THRESHOLD
import com.dluvian.nozzle.model.Offline
import com.dluvian.nozzle.model.Online
import com.dluvian.nozzle.model.OnlineStatus
import com.dluvian.nozzle.model.Waiting
import com.dluvian.nozzle.ui.theme.HintGray
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun OnlineStatusIndicator(
    onlineStatus: OnlineStatus,
    modifier: Modifier = Modifier,
    text: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OnlineStatusDotOrProgress(onlineStatus = onlineStatus)
        Spacer(modifier = Modifier.width(spacing.medium))
        Text(
            modifier = modifier,
            text = text ?: when (onlineStatus) {
                is Waiting -> stringResource(id = R.string.waiting)
                is Online -> stringResource(id = R.string.online)
                is Offline -> stringResource(id = R.string.offline)
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OnlineStatusDotOrProgress(onlineStatus: OnlineStatus) {
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
