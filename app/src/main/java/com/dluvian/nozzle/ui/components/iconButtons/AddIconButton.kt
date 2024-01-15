package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.icons.AddIcon

@Composable
fun AddIconButton(onAdd: () -> Unit, description: String) {
    IconButton(onClick = onAdd) {
        AddIcon(description = description)
    }
}
