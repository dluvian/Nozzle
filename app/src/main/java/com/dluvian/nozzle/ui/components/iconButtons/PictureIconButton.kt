package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.theme.sizing

@Composable
fun PictureIconButton(
    pubkey: String,
    picture: String?,
    showProfilePicture: Boolean,
    trustType: TrustType,
    description: String?,
    onPictureClick: () -> Unit
) {
    IconButton(
        onClick = onPictureClick,
        modifier = Modifier.semantics(mergeDescendants = true) {
            onClick(
                label = description,
                action = {
                    onPictureClick()
                    true
                })
        }
    ) {
        ProfilePicture(
            modifier = Modifier.size(sizing.smallProfilePicture),
            pubkey = pubkey,
            picture = picture,
            showProfilePicture = showProfilePicture,
            trustType = trustType
        )
    }
}
