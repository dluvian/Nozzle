package com.dluvian.nozzle.ui.components.postCard.atoms.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.postCard.atoms.CenteredBox
import com.dluvian.nozzle.ui.theme.FailedIcon

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
            LoadedMedia(
                modifier = Modifier.fillMaxWidth(),
                subModifier = subModifier,
                mediaUrl = mediaUrl,
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

@Composable
private fun LoadedMedia(
    mediaUrl: String,
    modifier: Modifier = Modifier,
    subModifier: Modifier = Modifier,
) {
    LoadedImage(modifier = modifier, subModifier = subModifier, mediaUrl = mediaUrl)
}

@Composable
private fun LoadedImage(modifier: Modifier, subModifier: Modifier, mediaUrl: String) {
    SubcomposeAsyncImage(
        modifier = modifier.clickable { /* Prevents opening post card */ },
        model = ImageRequest.Builder(LocalContext.current)
            .data(mediaUrl)
            .crossfade(true)
            .build(),
        loading = {
            CenteredBox(modifier = subModifier) {
                CircularProgressIndicator()
            }
        },
        error = {
            CenteredBox(modifier = subModifier) {
                Icon(imageVector = FailedIcon, contentDescription = null)
            }
        },
        contentScale = ContentScale.FillWidth,
        contentDescription = stringResource(id = R.string.loaded_media)
    )
}
