package com.dluvian.nozzle.ui.components.interactors

import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.text.NamedItem

@Composable
fun NamedRadio(
    isSelected: Boolean,
    name: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    NamedItem(
        item = {
            RadioButton(
                selected = isSelected,
                enabled = isEnabled,
                onClick = onClick,
            )
        },
        name = name
    )
}
