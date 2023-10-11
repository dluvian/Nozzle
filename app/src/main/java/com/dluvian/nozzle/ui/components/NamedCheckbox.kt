package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun NamedCheckbox(isChecked: Boolean, name: String, isEnabled: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            modifier = Modifier.size(sizing.smallIcon),
            enabled = isEnabled,
            checked = isChecked,
            onCheckedChange = { onClick() }
        )
        Spacer(modifier = Modifier.width(spacing.large))
        Text(text = name)
    }
}