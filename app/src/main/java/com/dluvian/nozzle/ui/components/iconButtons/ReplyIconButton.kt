package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.ReplyIcon

@Composable
fun ReplyIconButton(
    onReply: () -> Unit,
    description: String,
    modifier: Modifier = Modifier
) {
    BaseIconButton(
        modifier = modifier,
        imageVector = ReplyIcon,
        description = description,
        onClick = onReply
    )
}
