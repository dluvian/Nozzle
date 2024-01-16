package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.SaveIcon

@Composable
fun SaveIconButton(onSave: () -> Unit, description: String) {
    IconButton(onClick = onSave) {
        Icon(imageVector = SaveIcon, contentDescription = description)
    }
}
