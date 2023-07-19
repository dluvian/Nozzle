package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun InputBox(
    picture: String,
    pubkey: String,
    placeholder: String,
    onChangeInput: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            ProfilePicture(
                modifier = Modifier
                    .padding(start = spacing.screenEdge, top = spacing.large)
                    .size(sizing.profilePicture),
                pictureUrl = picture,
                pubkey = pubkey,
                showFriendIndicator = false
            )
            ChangeableTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                maxLines = Int.MAX_VALUE,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = placeholder,
                onChangeValue = onChangeInput,
                keyboardImeAction = ImeAction.Default
            )
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
