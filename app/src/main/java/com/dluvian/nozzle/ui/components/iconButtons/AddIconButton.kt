package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.AddIcon

@Composable
fun AddIconButton(onAdd: () -> Unit, description: String, modifier: Modifier = Modifier) {
    BaseIconButton(
        modifier = modifier,
        imageVector = AddIcon,
        description = description,
        onClick = onAdd
    )
}
