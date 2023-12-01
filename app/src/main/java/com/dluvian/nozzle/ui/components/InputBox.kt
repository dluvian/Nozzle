package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.utils.replaceWithNpub
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.ui.components.itemRow.ItemRow
import com.dluvian.nozzle.ui.components.itemRow.PictureAndName
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.components.postCard.AnnotatedMentionedPostCard
import com.dluvian.nozzle.ui.components.postCard.atoms.BorderedCard
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun InputBox(
    input: MutableState<TextFieldValue>,
    pubkey: String,
    placeholder: String,
    postToQuote: AnnotatedMentionedPost? = null,
    searchSuggestions: List<SimpleProfile> = listOf(
        SimpleProfile(
            name = "name",
            pubkey = "e4336cd525df79fa4d3af364fd9600d4b10dce4215aa4c33ed77ea0842344b10",
            trustScore = 0.5f,
            isOneself = false,
            isFollowedByMe = false
        ),
        SimpleProfile(
            name = "name",
            pubkey = "e4336cd525df79fa4d3af364fd9600d4b10dce4215aa4c33ed77ea0842344b10",
            trustScore = 0.5f,
            isOneself = false,
            isFollowedByMe = false
        ),
    ),
    onSearch: (String) -> Unit = {},
) {
    val showSuggestions = remember { mutableStateOf(false) }
    remember(input.value) {
        val current = input.value
        val stringUntilCursor = current.text.take(current.selection.end)
        val mentionedName = stringUntilCursor.takeLastWhile { it != '@' }
        if (mentionedName.any { it.isWhitespace() }) {
            showSuggestions.value = false
            return@remember false
        }
        showSuggestions.value = stringUntilCursor.contains("@")
        if (showSuggestions.value) onSearch(mentionedName)
        true
    }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(weight = 1f, fill = false)) {
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
                    maxLines = 4,
                    onNavigateToId = { /* Do nothing. Stay in PostScreen */ },
                )
            }
        }
        if (searchSuggestions.isNotEmpty() && showSuggestions.value) {
            SearchSuggestions(
                modifier = Modifier.weight(weight = 1f, fill = false),
                suggestions = searchSuggestions,
                onAddNpub = { npub ->
                    input.value = input.value.replaceWithNpub(npub = npub)
                }
            )
        }
    }
}

@Composable
private fun SearchSuggestions(
    suggestions: List<SimpleProfile>,
    onAddNpub: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BorderedCard(modifier = modifier) {
        LazyColumn(modifier = Modifier.wrapContentHeight()) {
            items(items = suggestions) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ItemRow(
                        content = { PictureAndName(profile = it, onNavigateToProfile = { }) },
                        onClick = { onAddNpub(EncodingUtils.hexToNpub(it.pubkey)) },
                    )
                }
            }
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
