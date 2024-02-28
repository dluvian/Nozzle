package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BaseIconButton(
    imageVector: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(modifier = iconModifier, imageVector = imageVector, contentDescription = description)
    }
}
