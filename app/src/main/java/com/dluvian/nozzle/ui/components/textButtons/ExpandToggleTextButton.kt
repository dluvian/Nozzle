package com.dluvian.nozzle.ui.components.textButtons

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.components.icons.toggle.ExpandOrCollapseIcon

@Composable
fun ExpandToggleTextButton(
    text: String,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onToggle
    ) {
        Text(text = text)
        ExpandOrCollapseIcon(isExpanded = isExpanded)
    }
}
