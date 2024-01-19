package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.SaveIcon

@Composable
fun SaveIconButton(onSave: () -> Unit, description: String) {
    BaseIconButton(imageVector = SaveIcon, description = description, onClick = onSave)
}
