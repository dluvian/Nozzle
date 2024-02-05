package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.theme.sizing

@Composable
fun PictureIconButton(
    pubkey: String,
    trustType: TrustType,
    description: String?,
    onPictureClick: () -> Unit
) {
    IconButton(
        onClick = onPictureClick,
        modifier = Modifier.clickable(onClickLabel = description, onClick = onPictureClick)
    ) {
        ProfilePicture(
            modifier = Modifier.size(sizing.smallProfilePicture),
            pubkey = pubkey,
            trustType = trustType
        )
    }
}
