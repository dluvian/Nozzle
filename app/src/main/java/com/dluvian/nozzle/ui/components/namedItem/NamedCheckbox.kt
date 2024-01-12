package com.dluvian.nozzle.ui.components.namedItem

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable

@Composable
fun NamedCheckbox(
    isChecked: Boolean,
    name: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    NamedItem(
        item = {
            Checkbox(
                enabled = isEnabled,
                checked = isChecked,
                onCheckedChange = { onClick() },
            )
        },
        name = name
    )
}
