package com.dluvian.nozzle.ui.components.hint

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R

@Composable
fun NoPostsHint() {
    BaseHint(text = stringResource(id = R.string.no_posts_found))
}