package com.dluvian.nozzle.ui.components.iconButtons.toggle

import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.icons.LikedIcon
import com.dluvian.nozzle.ui.components.icons.NotLikedIcon

@Composable
fun LikeToggleIconButton(
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onToggleLike) {
        if (isLiked) {
            LikedIcon(
                modifier = modifier,
                tint = Color.Red,
                description = stringResource(id = R.string.remove_like),
            )
        } else {
            NotLikedIcon(
                modifier = modifier,
                tint = MaterialTheme.colorScheme.onSurface,
                description = stringResource(id = R.string.like)
            )
        }
    }
}
