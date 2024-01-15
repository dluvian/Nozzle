package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.icons.DeleteIcon

@Composable
fun DeleteIconButton(onDelete: () -> Unit, description: String) {
    IconButton(onClick = onDelete) {
        DeleteIcon(description = description)
    }
}
