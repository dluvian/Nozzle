package com.dluvian.nozzle.ui.components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R

@Composable
fun EditProfileButton(onNavToEditProfile: () -> Unit) {
    HollowButton(
        text = stringResource(id = R.string.edit),
        onClick = onNavToEditProfile
    )
}
