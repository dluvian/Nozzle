package com.dluvian.nozzle.ui.components.bars

import androidx.compose.foundation.clickable
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.ui.components.iconButtons.PictureIconButton
import com.dluvian.nozzle.ui.components.iconButtons.SettingsIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedTopBar(
    pubkey: String,
    onOpenFilterDrawer: () -> Unit,
    onPictureClick: () -> Unit,
    onScrollToTop: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                modifier = Modifier.clickable(onClick = onScrollToTop),
                text = stringResource(id = R.string.Feed)
            )
        },
        navigationIcon = {
            PictureIconButton(pubkey = pubkey, trustType = Oneself, onPictureClick = onPictureClick)
        },
        actions = {
            SettingsIconButton(
                onClick = onOpenFilterDrawer,
                description = stringResource(id = R.string.feed_settings)
            )
        }
    )
}