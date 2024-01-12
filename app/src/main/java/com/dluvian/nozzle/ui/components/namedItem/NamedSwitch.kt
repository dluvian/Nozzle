package com.dluvian.nozzle.ui.components.namedItem

import androidx.compose.material.Switch
import androidx.compose.runtime.Composable

@Composable
fun NamedSwitch(
    isChecked: Boolean,
    name: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    NamedItem(
        item = {
            Switch(
                enabled = isEnabled,
                checked = isChecked,
                onCheckedChange = { onClick() },
            )
        },
        name = name
    )
}
