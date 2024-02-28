package com.dluvian.nozzle.ui.components.iconButtons.toggle

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.LikedIcon
import com.dluvian.nozzle.ui.theme.NotLikedIcon

@Composable
fun LikeToggleIconButton(
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) {
    IconButton(modifier = modifier, onClick = onToggleLike) {
        if (isLiked) {
            Icon(
                modifier = iconModifier,
                imageVector = LikedIcon,
                tint = Color.Red,
                contentDescription = stringResource(id = R.string.remove_like),
            )
        } else {
            Icon(
                modifier = iconModifier,
                imageVector = NotLikedIcon,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(id = R.string.like)
            )
        }
    }
}
