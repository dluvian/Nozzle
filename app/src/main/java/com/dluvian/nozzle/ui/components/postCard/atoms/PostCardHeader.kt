package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
    onOpenProfile: ((String) -> Unit)?
) {
    Row {
        Username(username = name, pubkey = pubkey, onOpenProfile = onOpenProfile)
        if (createdAt > 0) {
            Spacer(modifier = Modifier.width(spacing.medium))
            Bullet()
            Spacer(modifier = Modifier.width(spacing.medium))
            RelativeTime(from = createdAt)
        }
    }
}