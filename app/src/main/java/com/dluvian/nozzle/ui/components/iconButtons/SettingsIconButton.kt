package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.SettingsIcon

@Composable
fun SettingsIconButton(onClick: () -> Unit, description: String? = null) {
    IconButton(onClick = onClick) {
        Icon(imageVector = SettingsIcon, contentDescription = description)
    }
}
