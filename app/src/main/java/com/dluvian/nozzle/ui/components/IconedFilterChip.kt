package com.dluvian.nozzle.ui.components

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterialApi::class)
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
