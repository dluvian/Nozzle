package com.dluvian.nozzle.ui.app.views.editProfile

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Text
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
import com.dluvian.nozzle.ui.components.iconButtons.SaveIconButton
import com.dluvian.nozzle.ui.components.input.ChangeableTextField
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold
import com.dluvian.nozzle.ui.components.textButtons.ExpandToggleTextButton
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

    ReturnableScaffold(
        topBarText = stringResource(id = R.string.edit_profile),
        onGoBack = onGoBack,
        actions = {
            SaveIconButton(
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
                description = stringResource(id = R.string.save_profile)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = spacing.screenEdge)
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
    ExpandToggleTextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.medium),
        text = stringResource(id = R.string.advanced), isExpanded = isExpanded.value,
        onToggle = { isExpanded.value = !isExpanded.value },
    )
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

@Composable
private fun Username(username: MutableState<TextFieldValue>) {
    EditableField(
        text = stringResource(id = R.string.username),
        mutableInput = username,
        placeholder = stringResource(id = R.string.enter_your_username)
    )
}

@Composable
private fun About(about: MutableState<TextFieldValue>) {
    EditableField(
        text = stringResource(id = R.string.about_you),
        mutableInput = about,
        placeholder = stringResource(id = R.string.describe_yourself),
        maxLines = 3
    )
}

@Composable
private fun ProfilePictureUrl(pictureUrl: MutableState<TextFieldValue>) {
    EditableField(
        text = stringResource(id = R.string.profile_picture_url),
        mutableInput = pictureUrl,
        placeholder = stringResource(id = R.string.enter_a_picture_url),
        maxLines = 3,
        keyboardType = KeyboardType.Uri,
    )
}

@Composable
private fun Nip05(nip05: MutableState<TextFieldValue>) {
    EditableField(
        text = stringResource(id = R.string.nip05),
        mutableInput = nip05,
        placeholder = stringResource(id = R.string.enter_nip05),
        keyboardType = KeyboardType.Email,
    )
}

@Composable
private fun Lud16(lud16: MutableState<TextFieldValue>) {
    EditableField(
        text = stringResource(id = R.string.lightning_address),
        mutableInput = lud16,
        placeholder = stringResource(id = R.string.enter_lud16),
        keyboardType = KeyboardType.Email,
    )
}

@Composable
private fun EditableField(
    text: String,
    mutableInput: MutableState<TextFieldValue>,
    placeholder: String,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(text = text, fontWeight = FontWeight.Bold)
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        input = mutableInput,
        placeholder = placeholder,
        maxLines = maxLines,
        keyboardType = keyboardType
    )
}
