package com.dluvian.nozzle.ui.components.postCard.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dluvian.nozzle.ui.components.media.LoadedMedia
import com.dluvian.nozzle.ui.components.postCard.atoms.ShowMediaCard
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun MediaDecisionCard(
    mediaUrl: String,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean
) {
    // showMedia is not in PostWithMeta because the posts before infinite scroll activates are cold
    // to save resources
    val showMedia = remember(mediaUrl) { mutableStateOf(onShouldShowMedia(mediaUrl)) }
    if (!showMedia.value) {
        ShowMediaCard(onClick = {
            onShowMedia(mediaUrl)
            showMedia.value = true
        })
    } else {
        LoadedMedia(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = spacing.tiny, color = Color.LightGray),
            mediaUrl = mediaUrl
        )
    }
}
