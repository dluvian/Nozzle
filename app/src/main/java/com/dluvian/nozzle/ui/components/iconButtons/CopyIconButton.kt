package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.CopyIcon


@Composable
fun CopyIconButton(onCopy: () -> Unit, description: String) {
    IconButton(onClick = onCopy) {
        Icon(imageVector = CopyIcon, contentDescription = description)
    }
}
