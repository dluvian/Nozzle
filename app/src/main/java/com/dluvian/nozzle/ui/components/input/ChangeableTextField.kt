package com.dluvian.nozzle.ui.components.input

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun ChangeableTextField(
    modifier: Modifier = Modifier,
    input: MutableState<TextFieldValue> = remember { mutableStateOf(TextFieldValue()) },
    onChangeInput: ((String) -> Unit)? = null,
    maxLines: Int = 1,
    isError: Boolean = false,
    isTransparent: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardImeAction: ImeAction = ImeAction.Done,
    label: String? = null,
    errorLabel: String? = null,
    placeholder: String? = null,
    isPassword: Boolean = false,
    onImeAction: (() -> Unit)? = null,
    trailingIcon: @Composable() (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        modifier = modifier,
        value = input.value,
        isError = isError,
        maxLines = maxLines,
        placeholder = if (placeholder != null) {
            { Text(text = placeholder) }
        } else {
            null
        },
        label = if (isError && errorLabel != null) {
            { Text(text = errorLabel) }
        } else if (label != null && input.value.text.isNotEmpty()) {
            { Text(text = label) }
        } else null,
        onValueChange = { change ->
            input.value = change
            onChangeInput?.let { onChangeInput(input.value.text) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = keyboardImeAction,
            autoCorrect = false,
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() },
            onGo = { onImeAction?.let { it() } },
            onSearch = { onImeAction?.let { it() } },
        ),
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = trailingIcon,
        colors = if (isTransparent) TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent

        ) else TextFieldDefaults.colors()
    )
}
