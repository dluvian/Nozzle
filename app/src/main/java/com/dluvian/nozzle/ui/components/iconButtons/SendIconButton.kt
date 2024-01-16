package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.SendIcon

@Composable
fun SendIconButton(onSend: () -> Unit, description: String) {
    IconButton(onClick = onSend) {
        Icon(imageVector = SendIcon, contentDescription = description)
    }
}
