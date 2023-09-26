package com.dluvian.nozzle.ui.app.views.editProfile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.ChangeableTextField
import com.dluvian.nozzle.ui.components.CheckTopBarButton
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun EditProfileScreen(
    uiState: EditProfileViewModelState,
    onUpdateProfile: () -> Unit,
    onChangeName: (String) -> Unit,
    onChangeAbout: (String) -> Unit,
    onChangePicture: (String) -> Unit,
    onChangeNip05: (String) -> Unit,
    onChangeLud16: (String) -> Unit,
    onResetUiState: () -> Unit,
    onCanGoBack: () -> Boolean,
    onGoBack: () -> Unit,
) {
    val resetUI = remember { mutableStateOf(true) }
    if (resetUI.value) {
        onResetUiState()
        resetUI.value = false
    }

    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.edit_profile),
            onGoBack = onGoBack,
            trailingIcon = {
                CheckTopBarButton(
                    hasChanges = uiState.hasChanges,
                    onCheck = { onUpdateProfile() },
                    onCanGoBack = onCanGoBack,
                    onGoBack = onGoBack,
                )
            }
        )
        Column(
            modifier = Modifier
                .padding(spacing.screenEdge)
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Username(
                username = uiState.nameInput,
                onChangeName = onChangeName
            )
            Spacer(modifier = Modifier.height(spacing.xxl))

            About(about = uiState.aboutInput, onChangeAbout = onChangeAbout)
            Spacer(modifier = Modifier.height(spacing.xxl))

            ProfilePictureUrl(
                pictureUrl = uiState.pictureInput,
                isInvalid = uiState.isInvalidPictureUrl,
                onChangePicture = onChangePicture
            )
            Spacer(modifier = Modifier.height(spacing.xxl))

            Nip05(
                nip05 = uiState.nip05Input,
                onChangeNip05 = onChangeNip05
            )
            Spacer(modifier = Modifier.height(spacing.xxl))

            Lud16(
                lud16 = uiState.lud16Input,
                onChangeLud16 = onChangeLud16
            )
        }
    }
    DisposableEffect(key1 = null) {
        onDispose { onResetUiState() }
    }
}

@Composable
private fun Username(
    username: String,
    onChangeName: (String) -> Unit,
) {
    Text(text = stringResource(id = R.string.username), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        value = username,
        placeholder = stringResource(id = R.string.enter_your_username),
        onChangeValue = onChangeName,
    )
}

@Composable
private fun About(
    about: String,
    onChangeAbout: (String) -> Unit,
) {
    Text(text = stringResource(id = R.string.about_you), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        value = about,
        maxLines = 3,
        placeholder = stringResource(id = R.string.describe_yourself),
        onChangeValue = onChangeAbout,
    )
}

@Composable
private fun ProfilePictureUrl(
    pictureUrl: String,
    isInvalid: Boolean,
    onChangePicture: (String) -> Unit,
) {
    Text(text = stringResource(id = R.string.profile_picture_url), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        value = pictureUrl,
        isError = isInvalid,
        maxLines = 3,
        placeholder = stringResource(id = R.string.enter_a_picture_url),
        errorLabel = stringResource(id = R.string.invalid_url),
        keyboardType = KeyboardType.Uri,
        onChangeValue = onChangePicture,
    )
}

@Composable
private fun Nip05(
    nip05: String,
    onChangeNip05: (String) -> Unit,
) {
    Text(text = stringResource(id = R.string.nip05_identifier), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        value = nip05,
        maxLines = 3,
        placeholder = stringResource(id = R.string.enter_nip05),
        keyboardType = KeyboardType.Uri,
        onChangeValue = onChangeNip05,
    )
}

@Composable
private fun Lud16(
    lud16: String,
    onChangeLud16: (String) -> Unit,
) {
    Text(text = stringResource(id = R.string.lightning_address), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        value = lud16,
        maxLines = 3,
        placeholder = stringResource(id = R.string.enter_lud16),
        keyboardType = KeyboardType.Uri,
        onChangeValue = onChangeLud16,
    )
}
