package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun NamedCheckbox(
    isChecked: Boolean,
    name: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            enabled = isEnabled,
            checked = isChecked,
            onCheckedChange = { onClick() },
        )
        Text(text = name)
    }
}
