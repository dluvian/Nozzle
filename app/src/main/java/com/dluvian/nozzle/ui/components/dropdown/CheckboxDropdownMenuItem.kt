package com.dluvian.nozzle.ui.components.dropdown

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun CheckboxDropdownMenuItem(
    isChecked: Boolean,
    text: String,
    onToggle: () -> Unit,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues()
) {
    DropdownMenuItem(
        text = {
            Checkbox(
                checked = isChecked,
                enabled = enabled,
                onCheckedChange = { onToggle() })
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        onClick = onToggle,
        enabled = enabled,
        contentPadding = contentPadding
    )
}
