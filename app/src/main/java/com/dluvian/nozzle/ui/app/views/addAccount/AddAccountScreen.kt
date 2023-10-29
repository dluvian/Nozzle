package com.dluvian.nozzle.ui.app.views.addAccount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.ChangeableTextField
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.VisibilityIcon
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun AddAccountScreen(
    uiState: AddAccountViewModelState,
    onGenerateNew: () -> Unit,
    onLogin: (String) -> Unit,
    onGoBack: () -> Unit
) {
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.add_account),
            onGoBack = onGoBack
        )
        ScreenContent(
            value = uiState.value,
            isInvalid = uiState.isInvalid,
            onGenerateNew = onGenerateNew,
            onLogin = onLogin,
        )
    }
}

@Composable
private fun ScreenContent(
    value: String,
    isInvalid: Boolean,
    onGenerateNew: () -> Unit,
    onLogin: (String) -> Unit,
) {
    val isVisible = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val input = remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            elevation = spacing.small
        ) {
            Column {
                ChangeableTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    input = input,
                    placeholder = stringResource(id = R.string.nsec_ellipsis),
                    label = stringResource(id = R.string.nsec),
                    errorLabel = stringResource(id = R.string.invalid_nsec),
                    isPassword = !isVisible.value,
                    maxLines = 4,
                    isError = isInvalid,
                    trailingIcon = {
                        VisibilityIcon(
                            isVisible = isVisible.value,
                            onToggle = { isVisible.value = !isVisible.value }
                        )
                    }
                )
                Buttons(onGenerateNew = onGenerateNew, onLogin = { onLogin(input.value.text) })
            }
        }
    }
}

@Composable
private fun Buttons(onGenerateNew: () -> Unit, onLogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            modifier = Modifier.padding(end = spacing.medium),
            onClick = onGenerateNew
        ) {
            Text(stringResource(id = R.string.generate_new))
        }
        Spacer(modifier = Modifier.width(spacing.medium))
        TextButton(
            modifier = Modifier.padding(end = spacing.medium),
            onClick = onLogin
        ) {
            Text(stringResource(id = R.string.login))
        }
    }
}
