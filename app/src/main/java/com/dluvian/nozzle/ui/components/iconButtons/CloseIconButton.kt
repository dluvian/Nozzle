package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.icons.CloseIcon

@Composable
fun CloseIconButton(
    onClose: () -> Unit,
    description: String = stringResource(id = R.string.close)
) {
    IconButton(onClick = onClose) {
        CloseIcon(description = description)
    }
}
