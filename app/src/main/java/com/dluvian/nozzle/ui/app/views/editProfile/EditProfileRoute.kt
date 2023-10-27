package com.dluvian.nozzle.ui.app.views.editProfile

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.nostr.Metadata


@Composable
fun EditProfileRoute(
    editProfileViewModel: EditProfileViewModel,
    onGoBack: () -> Unit,
) {
    val metadataState by editProfileViewModel.metadataState.collectAsState()
    val context = LocalContext.current

    EditProfileScreen(
        metadataState = metadataState ?: Metadata(),
        onUpsertProfile = { metadata ->
            editProfileViewModel.onUpsertProfile(metadata)
            Toast.makeText(
                context,
                context.getString(R.string.profile_updated),
                Toast.LENGTH_SHORT
            ).show()
        },
        onGoBack = onGoBack,
    )
}
