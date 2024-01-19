package com.dluvian.nozzle.ui.components.postCard.atoms.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R

@Composable
fun ClickToLoadMoreCard(onClick: () -> Unit) {
    NoParentCard(text = stringResource(id = R.string.click_to_load_more), onClick = onClick)
}
