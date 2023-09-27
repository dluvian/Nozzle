package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun ChangeableTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    maxLines: Int = 1,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardImeAction: ImeAction = ImeAction.Done,
    errorLabel: String? = null,
    placeholder: String? = null,
    isPassword: Boolean = false,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    onChangeValue: (String) -> Unit,
    onImeAction: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    var newValue by remember { mutableStateOf(TextFieldValue(value)) }
    TextField(
        modifier = modifier,
        value = newValue,
        isError = isError,
        maxLines = maxLines,
        placeholder = if (placeholder != null) {
            { Text(text = placeholder) }
        } else {
            null
        },
        label = if (isError && errorLabel != null) {
            { Text(text = errorLabel) }
        } else {
            null
        },
        onValueChange = { change ->
            newValue = change
            onChangeValue(newValue.text)
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
        colors = colors,
    )
}
