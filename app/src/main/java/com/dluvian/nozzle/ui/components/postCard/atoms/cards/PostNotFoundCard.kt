package com.dluvian.nozzle.ui.components.postCard.atoms.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R

@Composable
fun PostNotFound() {
    NoParentCard(text = stringResource(id = R.string.post_not_found))
}
