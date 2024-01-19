package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.ReturnIcon

@Composable
fun GoBackIconButton(
    onGoBack: () -> Unit,
    description: String = stringResource(id = R.string.return_back)
) {
    BaseIconButton(imageVector = ReturnIcon, description = description, onClick = onGoBack)
}
