package com.dluvian.nozzle.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R

@Composable
fun getYesOrNo(value: Boolean?): String? {
    return when (value) {
        null -> null
        true -> stringResource(id = R.string.yes)
        false -> stringResource(id = R.string.no)
    }
}

@Composable
fun getStrOrUnknown(value: String?): String {
    return value ?: stringResource(id = R.string.unknown)
}
