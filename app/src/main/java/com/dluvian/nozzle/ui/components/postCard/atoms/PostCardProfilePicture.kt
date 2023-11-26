package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.components.media.ProfilePicture


@Composable
fun PostCardProfilePicture(
    pubkey: String,
    trustType: TrustType,
    onNavigateToProfile: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ProfilePicture(
        modifier = modifier,
        pubkey = pubkey,
        trustType = trustType,
        onOpenProfile = if (onNavigateToProfile != null) {
            { onNavigateToProfile(pubkey) }
        } else {
            null
        }
    )
}