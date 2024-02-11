package com.dluvian.nozzle.ui.components.interactors

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.text.NamedItem

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
                checked = isChecked,
                enabled = isEnabled,
                onCheckedChange = { onClick() },
            )
        },
        name = name,
    )
}
