package com.dluvian.nozzle.ui.components.text

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.data.utils.getRelativeTimeSpanString
import com.dluvian.nozzle.ui.theme.hintGray

@Composable
fun RelativeTime(from: Long, color: Color = MaterialTheme.colors.hintGray) {
    Text(
        text = getRelativeTimeSpanString(
            context = LocalContext.current,
            from = from
        ),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
