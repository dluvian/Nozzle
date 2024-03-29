package com.dluvian.nozzle.ui.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.getDefaultPictureBrush
import com.dluvian.nozzle.model.FollowedByFriend
import com.dluvian.nozzle.model.Friend
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.model.Unknown
import com.dluvian.nozzle.ui.theme.Orange500
import com.dluvian.nozzle.ui.theme.UnknownIcon
import com.dluvian.nozzle.ui.theme.VerifiedUserIcon

@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    pubkey: String,
    picture: String?,
    showProfilePicture: Boolean,
    trustType: TrustType,
    onOpenProfile: (() -> Unit)? = null,
) {
    BaseProfilePicture(
        modifier = modifier,
        pubkey = pubkey,
        picture = if (showProfilePicture) picture else null,
        trustType = trustType,
        description = stringResource(id = R.string.profile_picture),
        onOpenProfile = onOpenProfile,
    )
}

@Composable
private fun BaseProfilePicture(
    modifier: Modifier = Modifier,
    pubkey: String,
    picture: String?,
    trustType: TrustType,
    description: String,
    onOpenProfile: (() -> Unit)? = null,
) {
    Box(modifier = modifier) {
        val imgModifier = Modifier
            .clip(CircleShape)
            .fillMaxSize()
            .let {
                if (onOpenProfile != null) it.clickable(onClick = onOpenProfile)
                else it
            }
        SubcomposeAsyncImage(
            modifier = imgModifier,
            model = ImageRequest.Builder(LocalContext.current)
                .data(picture)
                .crossfade(true)
                .size(300)
                .build(),
            contentScale = ContentScale.Crop,
            loading = {
                DefaultPicture(
                    modifier = imgModifier,
                    pubkey = pubkey,
                    description = description
                )
            },
            error = {
                DefaultPicture(
                    modifier = imgModifier,
                    pubkey = pubkey,
                    description = description
                )
            },
            contentDescription = description
        )
        PictureIndicator(modifier = Modifier.fillMaxWidth(), trustType = trustType)
    }
}

@Composable
private fun DefaultPicture(pubkey: String, description: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .background(brush = getDefaultPictureBrush(pubkey = pubkey))
        .semantics { this.onClick(label = description, action = null) })
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
            imageVector = VerifiedUserIcon,
            trustScore = null
        )

        is FollowedByFriend -> PictureIndicatorBase(
            modifier = modifier,
            color = Orange500,
            imageVector = VerifiedUserIcon,
            trustScore = trustType.trustScore
        )

        is Unknown -> PictureIndicatorBase(
            modifier = modifier,
            color = Color.Gray,
            imageVector = UnknownIcon,
            trustScore = null
        )
    }
}

@Composable
private fun PictureIndicatorBase(
    color: Color,
    imageVector: ImageVector,
    trustScore: Float?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1.0f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1.0f)
                    .align(Alignment.TopEnd)
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
                            color = Color.White,
                            radius = size.minDimension * 0.33f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .aspectRatio(1.0f),
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = color
                )
            }
        }
    }
}
