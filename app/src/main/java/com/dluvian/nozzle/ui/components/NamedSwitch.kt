package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun NamedSwitch(
    isChecked: Boolean,
    name: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(
            enabled = isEnabled,
            checked = isChecked,
            onCheckedChange = { onClick() },
        )
        Text(text = name)
    }
}