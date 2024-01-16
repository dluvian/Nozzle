package com.dluvian.nozzle.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dluvian.nozzle.R


@Composable
fun FollowButton(
    isFollowed: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit
) {
    Button(
        onClick = if (isFollowed) onUnfollow else onFollow,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
        colors =
        ButtonDefaults.outlinedButtonColors(
            contentColor = if (isFollowed) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
            containerColor = if (isFollowed) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
        )

    ) {
        if (isFollowed) Text(text = stringResource(id = R.string.following))
        else Text(text = stringResource(id = R.string.follow))
    }
}
