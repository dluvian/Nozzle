package com.dluvian.nozzle.ui.app.views.editProfile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


// TODO: USE FLOWS
@Composable
fun EditProfileRoute(
    editProfileViewModel: EditProfileViewModel,
    onResetDrawerUiState: () -> Unit,
    onResetFeedIconUiState: () -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by editProfileViewModel.uiState.collectAsState()

    EditProfileScreen(
        uiState = uiState,
        onUpdateProfile = {
            // TODO: Simplify with Flows
            editProfileViewModel.onUpdateProfile()
            onResetDrawerUiState()
            onResetFeedIconUiState()
        },
        onChangeName = editProfileViewModel.onChangeName,
        onChangeAbout = editProfileViewModel.onChangeAbout,
        onChangePicture = editProfileViewModel.onChangePicture,
        onChangeNip05 = editProfileViewModel.onChangeNip05,
        onChangeLud16 = editProfileViewModel.onChangeLud16,
        onResetUiState = editProfileViewModel.onResetUiState,
        onCanGoBack = editProfileViewModel.onCanGoBack,
        onGoBack = onGoBack,
    )
}
