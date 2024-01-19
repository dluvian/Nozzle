package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.DeleteIcon

@Composable
fun DeleteIconButton(onDelete: () -> Unit, description: String) {
    BaseIconButton(imageVector = DeleteIcon, description = description, onClick = onDelete)
}
