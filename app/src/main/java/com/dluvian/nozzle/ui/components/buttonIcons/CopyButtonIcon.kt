package com.dluvian.nozzle.ui.components.buttonIcons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.icons.CopyIcon


@Composable
fun CopyButtonIcon(onCopy: () -> Unit) {
    IconButton(onClick = onCopy) {
        CopyIcon()
    }
}