package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.RelayIcon

@Composable
fun RelayIconButton(onClick: () -> Unit, description: String) {
    BaseIconButton(imageVector = RelayIcon, description = description, onClick = onClick)
}
