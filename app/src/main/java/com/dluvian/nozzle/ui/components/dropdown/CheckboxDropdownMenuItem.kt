package com.dluvian.nozzle.ui.components.dropdown

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.ui.theme.BoldStyle

@Composable
fun CheckboxDropdownMenuItem(
    isChecked: Boolean,
    text: String,
    onToggle: () -> Unit,
    count: Int? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues()
) {
    DropdownMenuItem(
        onClick = onToggle,
        enabled = enabled,
        contentPadding = contentPadding
    ) {
        Checkbox(
            checked = isChecked,
            enabled = enabled,
            onCheckedChange = { onToggle() })
        Text(
            text = buildAnnotatedString {
                append(text)
                count?.let {
                    if (it > 0) {
                        val num = " ($it)"
                        append(num)
                        addStyle(
                            style = BoldStyle,
                            start = text.length,
                            end = text.length + num.length,
                        )
                    }
                }
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
