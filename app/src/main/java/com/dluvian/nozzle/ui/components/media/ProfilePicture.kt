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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dluvian.nozzle.data.utils.getDefaultPictureBrush
import com.dluvian.nozzle.model.FollowedByFriend
import com.dluvian.nozzle.model.Friend
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.model.Unknown
import com.dluvian.nozzle.ui.theme.Orange500

@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    pubkey: String,
    trustType: TrustType,
    onOpenProfile: (() -> Unit)? = null,
) {
    BaseProfilePicture(
        modifier = modifier,
        pubkey = pubkey,
        trustType = trustType,
        onOpenProfile = onOpenProfile,
    )
}

@Composable
private fun BaseProfilePicture(
    modifier: Modifier = Modifier,
    pubkey: String,
    trustType: TrustType,
    onOpenProfile: (() -> Unit)? = null,
) {
    Box(modifier = modifier) {
        DefaultPicture(
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxSize()
                .let {
                    if (onOpenProfile != null) it.clickable(onClick = onOpenProfile)
                    else it
                },
            pubkey = pubkey
        )
        PictureIndicator(modifier = Modifier.fillMaxWidth(), trustType = trustType)
    }
}

@Composable
private fun DefaultPicture(pubkey: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(brush = getDefaultPictureBrush(pubkey = pubkey)))
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
            imageVector = Icons.Filled.Verified,
            trustScore = null
        )

        is FollowedByFriend -> PictureIndicatorBase(
            modifier = modifier,
            color = Orange500,
            imageVector = Icons.Filled.VerifiedUser,
            trustScore = trustType.trustScore
        )

        is Unknown -> PictureIndicatorBase(
            modifier = modifier,
            color = Color.Gray,
            imageVector = Icons.AutoMirrored.Filled.Help,
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
