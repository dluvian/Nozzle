package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.SendIcon

@Composable
fun SendIconButton(onSend: () -> Unit, description: String) {
    BaseIconButton(imageVector = SendIcon, description = description, onClick = onSend)
}
