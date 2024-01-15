package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.icons.RelayIcon

@Composable
fun RelayIconButton(onClick: () -> Unit, description: String) {
    IconButton(onClick = onClick) {
        RelayIcon(description = description)
    }
}
