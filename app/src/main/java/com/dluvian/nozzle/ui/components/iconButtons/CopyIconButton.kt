package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.CopyIcon


@Composable
fun CopyIconButton(onCopy: () -> Unit, description: String) {
    BaseIconButton(imageVector = CopyIcon, description = description, onClick = onCopy)
}
