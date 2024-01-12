package com.dluvian.nozzle.ui.components.chips

import androidx.compose.material.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun IconedFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    leadingIcon: ImageVector?,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        leadingIcon = {
            leadingIcon?.let { icon -> Icon(imageVector = icon, contentDescription = null) }
        }
    ) {
        Text(text = text)
    }
}
