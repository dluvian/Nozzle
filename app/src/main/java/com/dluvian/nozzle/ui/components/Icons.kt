package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.sizing

@Composable
fun CopyIcon(onCopy: () -> Unit) {
    CopyIcon(
        modifier = Modifier
            .size(sizing.smallItem)
            .clickable { onCopy() },
        description = stringResource(id = R.string.copy_content),
    )
}

@Composable
fun CopyIcon(
    modifier: Modifier = Modifier,
    description: String = stringResource(id = R.string.copy_content),
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.ContentCopy,
        contentDescription = description,
        tint = tint,
    )
}

@Composable
fun SearchIcon(
    modifier: Modifier = Modifier,
    description: String? = null,
    tint: Color = colors.onBackground,
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Rounded.Search,
        contentDescription = description,
        tint = tint
    )
}

@Composable
fun ReplyIcon(
    modifier: Modifier = Modifier,
    description: String? = stringResource(id = R.string.reply),
    tint: Color = colors.onBackground,
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Outlined.Chat,
        contentDescription = description,
        tint = tint
    )
}

@Composable
fun QuoteIcon(
    modifier: Modifier = Modifier,
    description: String? = stringResource(id = R.string.quote),
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.FormatQuote,
        contentDescription = description,
    )
}

@Composable
fun LikeIcon(
    isLiked: Boolean,
    modifier: Modifier = Modifier,
    description: String? = stringResource(id = R.string.like),
    activeTint: Color = Color.Red,
    inactiveTint: Color = colors.onBackground,
) {
    Icon(
        modifier = modifier,
        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        contentDescription = description,
        tint = if (isLiked) activeTint else inactiveTint,
    )
}

@Composable
fun AddIcon(
    modifier: Modifier = Modifier,
    description: String? = stringResource(id = R.string.add),
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.Add,
        contentDescription = description,
    )
}

@Composable
fun DeleteIcon(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    description: String? = stringResource(id = R.string.delete),
) {
    Icon(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onDelete),
        imageVector = Icons.Default.Delete,
        contentDescription = description,
    )
}

@Composable
fun VisibilityIcon(isVisible: Boolean, onToggle: () -> Unit) {
    Icon(
        modifier = Modifier
            .size(sizing.smallItem)
            .clickable { onToggle() },
        imageVector = if (isVisible) {
            Icons.Default.VisibilityOff
        } else {
            Icons.Default.Visibility
        },
        contentDescription = stringResource(id = R.string.toggle_visibility),
    )
}
