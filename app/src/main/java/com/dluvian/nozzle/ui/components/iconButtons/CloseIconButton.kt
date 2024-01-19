package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.CloseIcon

@Composable
fun CloseIconButton(
    onClose: () -> Unit,
    description: String = stringResource(id = R.string.close)
) {
    BaseIconButton(imageVector = CloseIcon, description = description, onClick = onClose)
}
