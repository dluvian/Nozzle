package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.AddIcon

@Composable
fun AddIconButton(onAdd: () -> Unit, description: String) {
    BaseIconButton(imageVector = AddIcon, description = description, onClick = onAdd)
}
