package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.icons.CopyIcon


@Composable
fun CopyIconButton(onCopy: () -> Unit, description: String) {
    IconButton(onClick = onCopy) {
        CopyIcon(description = description)
    }
}
