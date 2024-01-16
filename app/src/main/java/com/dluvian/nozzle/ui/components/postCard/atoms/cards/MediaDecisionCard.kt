package com.dluvian.nozzle.ui.components.postCard.atoms.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.media.LoadedImage
import com.dluvian.nozzle.ui.components.postCard.atoms.CenteredBox

@Composable
fun MediaDecisionCard(
    mediaUrl: String,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    // showMedia is not in PostWithMeta because the posts before infinite scroll activates are cold
    // to save resources
    BorderedCard(modifier = modifier) {
        val showMedia = remember(mediaUrl) { mutableStateOf(onShouldShowMedia(mediaUrl)) }
        val subModifier = remember {
            Modifier
                .fillMaxWidth()
                .aspectRatio(6f)
        }

        if (!showMedia.value) {
            ShowMediaCard(
                modifier = subModifier,
                onClick = {
                    onShowMedia(mediaUrl)
                    showMedia.value = true
                })
        } else {
            LoadedImage(
                modifier = Modifier.fillMaxWidth(),
                subModifier = subModifier,
                mediaUrl = mediaUrl
            )
        }
    }
}

@Composable
private fun ShowMediaCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CenteredBox(modifier = modifier.clickable(onClick = onClick)) {
        Text(
            modifier = Modifier.clickable(onClick = onClick),
            text = stringResource(id = R.string.click_to_show_media),
        )
    }
}
