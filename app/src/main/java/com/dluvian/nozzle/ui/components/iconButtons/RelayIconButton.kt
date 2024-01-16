package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.RelayIcon

@Composable
fun RelayIconButton(onClick: () -> Unit, description: String) {
    IconButton(onClick = onClick) {
        Icon(imageVector = RelayIcon, contentDescription = description)
    }
}