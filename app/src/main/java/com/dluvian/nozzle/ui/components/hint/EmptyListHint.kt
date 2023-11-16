package com.dluvian.nozzle.ui.components.hint

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R

@Composable
fun EmptyListHint() {
    BaseHint(text = stringResource(id = R.string.list_is_empty))
}
