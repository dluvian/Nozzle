package com.dluvian.nozzle.ui.components.fabs

import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.icons.WriteIcon

@Composable
fun CreateNoteFab(onCreateNote: () -> Unit) {
    FloatingActionButton(onClick = onCreateNote) {
        WriteIcon(description = stringResource(id = R.string.create_note))
    }
}