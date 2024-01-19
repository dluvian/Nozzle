package com.dluvian.nozzle.ui.components.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.data.utils.getRelativeTimeSpanString

@Composable
fun RelativeTime(from: Long) {
    Text(
        text = getRelativeTimeSpanString(
            context = LocalContext.current,
            from = from
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
