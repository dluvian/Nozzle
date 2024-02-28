package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.ReplyIcon

@Composable
fun ReplyIconButton(
    onReply: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) {
    BaseIconButton(
        modifier = modifier,
        iconModifier = iconModifier,
        imageVector = ReplyIcon,
        description = description,
        onClick = onReply
    )
}
