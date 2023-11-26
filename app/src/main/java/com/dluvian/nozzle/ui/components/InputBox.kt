package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.components.postCard.AnnotatedMentionedPostCard
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun InputBox(
    input: MutableState<TextFieldValue>,
    pubkey: String,
    placeholder: String,
    postToQuote: AnnotatedMentionedPost? = null
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BaseInputBox(
            modifier = Modifier.weight(weight = 1f, fill = false),
            input = input,
            pubkey = pubkey,
            placeholder = placeholder,
        )
        postToQuote?.let { quote ->
            AnnotatedMentionedPostCard(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(spacing.screenEdge),
                post = quote,
                onNavigateToId = { /* Do nothing. Stay in PostScreen */ },
            )
        }
    }
}

@Composable
private fun BaseInputBox(
    input: MutableState<TextFieldValue>,
    pubkey: String,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfilePicture(
                modifier = Modifier
                    .padding(start = spacing.screenEdge, top = spacing.large)
                    .size(sizing.profilePicture),
                pubkey = pubkey,
                trustType = Oneself
            )
            ChangeableTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .focusRequester(focusRequester),
                input = input,
                maxLines = Int.MAX_VALUE,
                keyboardImeAction = ImeAction.Default,
                placeholder = placeholder,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
            )
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
