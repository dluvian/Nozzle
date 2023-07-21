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
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.getRobohashUrl
import com.dluvian.nozzle.data.utils.hexToNpub
import com.dluvian.nozzle.model.FollowedByFriend
import com.dluvian.nozzle.model.Friend
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.model.Unknown
import com.dluvian.nozzle.ui.theme.Orange500

@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    pictureUrl: String,
    pubkey: String,
    trustType: TrustType,
    onOpenProfile: (() -> Unit)? = null,
) {
    BaseProfilePicture(
        modifier = modifier,
        pictureUrl = pictureUrl.ifEmpty { getRobohashUrl(hexToNpub(pubkey)) },
        trustType = trustType,
        onOpenProfile = onOpenProfile,
    )
}

@Composable
fun BaseProfilePicture(
    modifier: Modifier = Modifier,
    pictureUrl: String,
    trustType: TrustType,
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
        PictureIndicator(modifier = Modifier.fillMaxWidth(), trustType = trustType)
    }
}

@Composable
private fun PictureIndicator(
    trustType: TrustType,
    modifier: Modifier = Modifier
) {
    when (trustType) {
        is Oneself -> {}

        is Friend -> PictureIndicatorBase(
            modifier = modifier,
            color = Color.Green,
            imageVector = if (trustType.isVerified) Icons.Filled.Stars
            else Icons.Filled.VerifiedUser,
            trustScore = null
        )

        is FollowedByFriend -> PictureIndicatorBase(
            modifier = modifier,
            color = Orange500,
            imageVector = if (trustType.isVerified) Icons.Filled.Stars
            else Icons.Filled.VerifiedUser,
            trustScore = trustType.trustScore
        )

        is Unknown -> PictureIndicatorBase(
            modifier = modifier,
            color = if (trustType.isVerified) Orange500 else Color.Gray,
            imageVector = if (trustType.isVerified) Icons.Filled.Stars
            else Icons.Filled.Help,
            trustScore = null
        )

    }

}

// TODO: Sub to friends follower list for friends visible in feed
@Composable
private fun PictureIndicatorBase(
    color: Color,
    imageVector: ImageVector,
    trustScore: Float?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.End) {
        val bgColor = colors.background
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color = bgColor)
                .fillMaxWidth(0.35f)
                .aspectRatio(1.0f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color = color.copy(alpha = 0.2f))
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1.0f)
                    .let {
                        if (trustScore != null) {
                            it.drawBehind {
                                drawArc(
                                    Color.Green,
                                    startAngle = -90f,
                                    sweepAngle = trustScore * 360,
                                    useCenter = true,
                                )
                            }
                        } else it
                    }
                    .drawBehind {
                        drawCircle(
                            color = bgColor,
                            radius = size.minDimension * 0.33f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .aspectRatio(1.0f),
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = color
                )
            }
        }
    }
}
