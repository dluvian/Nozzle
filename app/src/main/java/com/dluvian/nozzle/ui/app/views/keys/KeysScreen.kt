package com.dluvian.nozzle.ui.app.views.keys

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.copyAndToast
import com.dluvian.nozzle.ui.components.iconButtons.CopyIconButton
import com.dluvian.nozzle.ui.components.iconButtons.toggle.VisibilityToggleIconButton
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun KeysScreen(
    uiState: KeysViewModelState,
    onGoBack: () -> Unit,
) {
    ReturnableScaffold(
        topBarText = stringResource(id = R.string.keys),
        onGoBack = onGoBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.screenEdge)
        ) {
            Npub(npub = uiState.npub)
            Spacer(modifier = Modifier.height(spacing.xxl))
            Nsec(nsec = uiState.nsec)
            Spacer(modifier = Modifier.height(spacing.large))
        }
    }
}

@Composable
private fun Npub(npub: String) {
    Text(
        text = stringResource(id = R.string.public_key),
        fontWeight = FontWeight.Bold
    )
    Text(text = stringResource(id = R.string.public_key_explanation))
    val clip = LocalClipboardManager.current
    val context = LocalContext.current
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = TextFieldValue(npub),
        enabled = false,
        onValueChange = { /* Always disabled*/ },
        trailingIcon = {
            CopyIconButton(
                onCopy = {
                    copyAndToast(
                        text = npub,
                        toast = context.getString(R.string.copied_public_key),
                        context = context,
                        clip = clip
                    )
                },
                description = stringResource(id = R.string.copy_public_key)
            )
        }
    )
}

@Composable
private fun Nsec(nsec: String) {
    Text(text = stringResource(id = R.string.private_key), fontWeight = FontWeight.Bold)
    Text(text = stringResource(id = R.string.private_key_description))
    Text(text = stringResource(id = R.string.private_key_warning))

    val isVisible = remember { mutableStateOf(false) }
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = nsec,
        onValueChange = {},
        maxLines = 4,
        enabled = false,
        visualTransformation = if (isVisible.value) VisualTransformation.None
        else PasswordVisualTransformation(),
        trailingIcon = {
            NsecTrailingIcons(
                nsec = nsec,
                isVisible = isVisible.value,
                onToggleVisibility = { isVisible.value = !isVisible.value })
        }
    )
}

@Composable
private fun NsecTrailingIcons(nsec: String, isVisible: Boolean, onToggleVisibility: () -> Unit) {
    val clip = LocalClipboardManager.current
    val context = LocalContext.current
    Row {
        VisibilityToggleIconButton(
            isVisible = isVisible,
            onToggleVisibility = onToggleVisibility
        )
        Spacer(modifier = Modifier.width(spacing.medium))
        CopyIconButton(
            onCopy = {
                copyAndToast(
                    text = nsec,
                    toast = context.getString(R.string.copied_private_key),
                    context = context,
                    clip = clip
                )
            },
            description = stringResource(id = R.string.copy_private_key)
        )
    }
}
