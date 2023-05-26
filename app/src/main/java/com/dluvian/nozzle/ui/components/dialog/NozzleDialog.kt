package com.dluvian.nozzle.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.dluvian.nozzle.ui.theme.sizing

@Composable
fun NozzleDialog(onCloseDialog: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onCloseDialog) {
        Column(verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.heightIn(max = sizing.dialogHeight)) {
                content()
            }
        }
    }
}
