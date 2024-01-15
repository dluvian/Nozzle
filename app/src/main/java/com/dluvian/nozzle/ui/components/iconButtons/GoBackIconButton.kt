package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.icons.ReturnIcon

@Composable
fun GoBackIconButton(
    onGoBack: () -> Unit,
    description: String = stringResource(id = R.string.return_back)
) {
    IconButton(onClick = onGoBack) {
        ReturnIcon(description = description)
    }
}
