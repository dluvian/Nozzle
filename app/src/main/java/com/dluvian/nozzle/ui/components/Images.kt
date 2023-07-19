package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.getRobohashUrl
import com.dluvian.nozzle.data.utils.hexToNpub

@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    pictureUrl: String,
    pubkey: String,
    showFriendIndicator: Boolean,
    onOpenProfile: (() -> Unit)? = null,
) {
    BaseProfilePicture(
        modifier = modifier,
        pictureUrl = pictureUrl.ifEmpty { getRobohashUrl(hexToNpub(pubkey)) },
        showFriendIndicator = showFriendIndicator,
        onOpenProfile = onOpenProfile,
    )
}

@Composable
fun BaseProfilePicture(
    modifier: Modifier = Modifier,
    pictureUrl: String,
    showFriendIndicator: Boolean,
    onError: (() -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null,
) {
    Box(modifier = modifier) {
        val imgModifier = Modifier
            .clip(CircleShape)
            .background(color = Color.LightGray)
            .fillMaxSize()
        AsyncImage(
            modifier = if (onOpenProfile != null)
                imgModifier.clickable { onOpenProfile() }
            else {
                imgModifier
            },
            model = ImageRequest.Builder(LocalContext.current)
                .data(pictureUrl)
                .crossfade(true)
                .size(300)
                .build(),
            onError = {
                if (onError != null) {
                    onError()
                }
            },
            error = painterResource(R.drawable.ic_default_profile),
            fallback = painterResource(R.drawable.ic_default_profile),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_default_profile),
            contentDescription = stringResource(id = R.string.profile_picture)
        )
        if (showFriendIndicator) PictureIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun PictureIndicator(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color = colors.background)
                .fillMaxWidth(0.33f)
                .aspectRatio(1.0f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1.0f),
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = colors.primaryVariant
            )
        }
    }
}
