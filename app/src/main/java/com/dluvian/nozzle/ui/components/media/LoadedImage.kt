package com.dluvian.nozzle.ui.components.media

import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
fun LoadedImage(modifier: Modifier, subModifier: Modifier, mediaUrl: String) {
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
