package com.dluvian.nozzle.ui.app.views.editProfile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.ui.components.ChangeableTextField
import com.dluvian.nozzle.ui.components.ExpandIcon
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.SaveIcon
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun EditProfileScreen(
    metadataState: Metadata,
    onUpsertProfile: (Metadata) -> Unit,
    onGoBack: () -> Unit,
) {
    val nameInput = remember(metadataState.name) {
        mutableStateOf(TextFieldValue(metadataState.name.orEmpty()))
    }
    val aboutInput = remember(metadataState.about) {
        mutableStateOf(TextFieldValue(metadataState.about.orEmpty()))
    }
    val pictureInput = remember(metadataState.picture) {
        mutableStateOf(TextFieldValue(metadataState.picture.orEmpty()))
    }
    val nip05Input = remember(metadataState.nip05) {
        mutableStateOf(TextFieldValue(metadataState.nip05.orEmpty()))
    }
    val lud16Input = remember(metadataState.lud16) {
        mutableStateOf(TextFieldValue(metadataState.lud16.orEmpty()))
    }

    val hasChanges = remember(
        nameInput.value.text,
        aboutInput.value.text,
        pictureInput.value.text,
        nip05Input.value.text,
        lud16Input.value.text,
    ) {
        nameInput.value.text != metadataState.name.orEmpty()
                || aboutInput.value.text != metadataState.about.orEmpty()
                || pictureInput.value.text != metadataState.picture.orEmpty()
                || nip05Input.value.text != metadataState.nip05.orEmpty()
                || lud16Input.value.text != metadataState.lud16.orEmpty()
    }

    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.edit_profile),
            onGoBack = onGoBack,
            trailingIcon = {
                if (hasChanges) {
                    SaveIcon(
                        onSave = {
                            onUpsertProfile(
                                Metadata(
                                    name = nameInput.value.text,
                                    about = aboutInput.value.text,
                                    picture = pictureInput.value.text,
                                    nip05 = nip05Input.value.text,
                                    lud16 = lud16Input.value.text
                                )
                            )
                            onGoBack()
                        },
                    )
                }
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
            Username(username = nameInput)
            Spacer(modifier = Modifier.height(spacing.xxl))
            About(about = aboutInput)

            Advanced(pictureInput = pictureInput, nip05Input = nip05Input, lud16Input = lud16Input)
        }
    }
}

@Composable
private fun Advanced(
    pictureInput: MutableState<TextFieldValue>,
    nip05Input: MutableState<TextFieldValue>,
    lud16Input: MutableState<TextFieldValue>
) {
    val isExpanded = remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                modifier = Modifier.padding(vertical = spacing.medium),
                onClick = { isExpanded.value = !isExpanded.value }
            ) {
                Text(text = stringResource(id = R.string.advanced))
                ExpandIcon(isExpanded = isExpanded.value)
            }
        }
        AnimatedVisibility(visible = isExpanded.value) {
            Column {
                ProfilePictureUrl(pictureUrl = pictureInput)
                Spacer(modifier = Modifier.height(spacing.xxl))

                Nip05(nip05 = nip05Input)
                Spacer(modifier = Modifier.height(spacing.xxl))

                Lud16(lud16 = lud16Input)
            }
        }
    }
}

@Composable
private fun Username(username: MutableState<TextFieldValue>) {
    Text(text = stringResource(id = R.string.username), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        input = username,
        placeholder = stringResource(id = R.string.enter_your_username),
    )
}

@Composable
private fun About(about: MutableState<TextFieldValue>) {
    Text(text = stringResource(id = R.string.about_you), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        input = about,
        maxLines = 3,
        placeholder = stringResource(id = R.string.describe_yourself),
    )
}

@Composable
private fun ProfilePictureUrl(pictureUrl: MutableState<TextFieldValue>) {
    Text(text = stringResource(id = R.string.profile_picture_url), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        input = pictureUrl,
        maxLines = 3,
        placeholder = stringResource(id = R.string.enter_a_picture_url),
        errorLabel = stringResource(id = R.string.invalid_url),
        keyboardType = KeyboardType.Uri,
    )
}

@Composable
private fun Nip05(nip05: MutableState<TextFieldValue>) {
    Text(text = stringResource(id = R.string.nip05), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        input = nip05,
        maxLines = 3,
        placeholder = stringResource(id = R.string.enter_nip05),
        keyboardType = KeyboardType.Uri,
    )
}

@Composable
private fun Lud16(lud16: MutableState<TextFieldValue>) {
    Text(text = stringResource(id = R.string.lightning_address), fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        input = lud16,
        maxLines = 3,
        placeholder = stringResource(id = R.string.enter_lud16),
        keyboardType = KeyboardType.Uri,
    )
}
