package com.dluvian.nozzle.ui.app.views.keys

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.*
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun KeysScreen(
    uiState: KeysViewModelState,
    onCopyNpub: () -> Unit,
    onUpdateKeyPair: (FocusManager) -> Unit,
    onChangePrivkey: (String) -> Unit,
    onResetUiState: () -> Unit,
    onGoBack: () -> Unit,
) {
    Column {
        val focusManager = LocalFocusManager.current
        ReturnableTopBar(
            text = stringResource(id = R.string.keys),
            onGoBack = onGoBack,
            trailingIcon = {
                CheckTopBarButton(
                    hasChanges = uiState.hasChanges,
                    onCheck = { onUpdateKeyPair(focusManager) },
                )
            })
        Column(modifier = Modifier.padding(spacing.screenEdge)) {
            Npub(
                npub = uiState.npub,
                onCopyNpub = onCopyNpub
            )
            Spacer(modifier = Modifier.height(spacing.xxl))
            Privkey(
                privkey = uiState.privkeyInput,
                isInvalid = uiState.isInvalid,
                onChangePrivkey = onChangePrivkey,
            )
            Spacer(modifier = Modifier.height(spacing.large))
        }
    }
    DisposableEffect(key1 = null) {
        onDispose { onResetUiState() }
    }
}

@Composable
private fun Npub(
    npub: String,
    onCopyNpub: () -> Unit
) {
    Text(
        text = stringResource(id = R.string.public_key),
        fontWeight = FontWeight.Bold
    )
    Text(text = stringResource(id = R.string.public_key_explanation))
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = TextFieldValue(npub),
        enabled = false,
        onValueChange = { /* Always disabled*/ },
        trailingIcon = { CopyIcon(onCopy = onCopyNpub) }
    )
}

@Composable
private fun Privkey(
    privkey: String,
    isInvalid: Boolean,
    onChangePrivkey: (String) -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }
    Text(
        text = stringResource(id = R.string.private_key),
        fontWeight = FontWeight.Bold
    )
    Text(text = stringResource(id = R.string.private_key_description))
    Text(text = stringResource(id = R.string.private_key_warning))
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        value = privkey,
        isError = isInvalid,
        maxLines = 4,
        placeholder = stringResource(id = R.string.enter_a_private_key),
        errorLabel = stringResource(id = R.string.invalid_private_key),
        isPassword = !isVisible,
        onChangeValue = onChangePrivkey,
        trailingIcon = {
            VisibilityIcon(
                isVisible = isVisible,
                onToggle = { isVisible = !isVisible })
        },
    )
}
