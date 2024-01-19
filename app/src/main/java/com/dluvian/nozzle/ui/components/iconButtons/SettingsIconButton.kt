package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.SettingsIcon

@Composable
fun SettingsIconButton(onClick: () -> Unit, description: String) {
    BaseIconButton(imageVector = SettingsIcon, description = description, onClick = onClick)
}
