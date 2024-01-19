package com.dluvian.nozzle.ui.components.fabs

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.WriteIcon

@Composable
fun CreateNoteFab(onCreateNote: () -> Unit) {
    FloatingActionButton(onClick = onCreateNote) {
        Icon(
            imageVector = WriteIcon,
            contentDescription = stringResource(id = R.string.create_note)
        )
    }
}
