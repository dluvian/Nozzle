package com.dluvian.nozzle.ui.components.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.buttons.BaseButton
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun AddingTextFieldWithButton(
    modifier: Modifier = Modifier,
    placeholder: String,
    isEnabled: Boolean,
    isError: Boolean,
    onAdd: (String) -> Boolean
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    Row(modifier = modifier) {
        TextField(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            value = text,
            enabled = isEnabled,
            isError = isError,
            maxLines = 1,
            placeholder = { Text(text = placeholder) },
            onValueChange = { change -> text = change },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
                autoCorrect = false,
            ),
            keyboardActions = KeyboardActions(onDone = { onAdd(text.text) }),
        )
        Spacer(modifier = Modifier.width(spacing.large))
        BaseButton(
            modifier = Modifier.fillMaxHeight(),
            text = stringResource(id = R.string.add),
            isEnabled = isEnabled,
            onClick = {
                val success = onAdd(text.text)
                if (success) text = TextFieldValue("")
            }
        )
    }

}
