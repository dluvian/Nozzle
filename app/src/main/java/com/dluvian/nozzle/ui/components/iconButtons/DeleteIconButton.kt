package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.DeleteIcon

@Composable
fun DeleteIconButton(onDelete: () -> Unit, description: String) {
    IconButton(onClick = onDelete) {
        Icon(imageVector = DeleteIcon, contentDescription = description)
    }
}
