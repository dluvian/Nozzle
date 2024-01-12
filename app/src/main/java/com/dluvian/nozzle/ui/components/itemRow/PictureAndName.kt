package com.dluvian.nozzle.ui.components.itemRow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.TrustType
import com.dluvian.nozzle.ui.components.postCard.atoms.PostCardProfilePicture
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun PictureAndName(profile: SimpleProfile, onNavigateToProfile: (Pubkey) -> Unit) {
    PostCardProfilePicture(
        modifier = Modifier.size(sizing.profilePicture),
        pubkey = profile.pubkey,
        trustType = TrustType.determineTrustType(
            isOneself = profile.isOneself,
            isFollowed = profile.isFollowedByMe,
            trustScore = profile.trustScore,
        ),
        onNavigateToProfile = onNavigateToProfile
    )
    Spacer(Modifier.width(spacing.large))
    Text(
        text = profile.name.ifBlank {
            ShortenedNameUtils.getShortenedNpubFromPubkey(profile.pubkey).orEmpty()
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
