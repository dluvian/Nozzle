package com.dluvian.nozzle.ui.components.iconButtons.toggle

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.icons.ExpandOrCollapseIcon

@Composable
fun ExpandToggleIconButton(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        ExpandOrCollapseIcon(isExpanded = isExpanded)
    }
}
