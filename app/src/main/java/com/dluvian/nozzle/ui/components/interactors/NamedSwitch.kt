package com.dluvian.nozzle.ui.components.interactors

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.text.NamedItem

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
