package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.components.text.Bullet
import com.dluvian.nozzle.ui.components.text.RelativeTime
import com.dluvian.nozzle.ui.components.text.Username
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun PostCardHeader(
    name: String,
    pubkey: String,
    createdAt: Long,
    onOpenProfile: ((String) -> Unit)?,
    showOptions: Boolean,
    onCopyId: () -> Unit = {},
    onCopyContent: () -> Unit = {},
    onFollow: (() -> Unit)? = null,
    onUnfollow: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.fillMaxWidth(0.9f)) {
            Username(username = name, onOpenProfile =
            if (onOpenProfile != null) {
                { onOpenProfile(pubkey) }
            } else null
            )
            if (createdAt > 0) {
                Spacer(modifier = Modifier.width(spacing.medium))
                Bullet()
                Spacer(modifier = Modifier.width(spacing.medium))
                RelativeTime(from = createdAt)
            }
        }

        if (showOptions) {
            OptionsButton(
                modifier = Modifier.weight(1f),
                onCopyId = onCopyId,
                onCopyContent = onCopyContent,
                onFollow = onFollow,
                onUnfollow = onUnfollow,
                onDelete = onDelete
            )
        }
    }
}
