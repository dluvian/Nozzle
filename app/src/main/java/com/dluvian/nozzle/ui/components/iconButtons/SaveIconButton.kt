package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.icons.SaveIcon

@Composable
fun SaveIconButton(onSave: () -> Unit, description: String) {
    IconButton(onClick = onSave) {
        SaveIcon(description = description)
    }
}
