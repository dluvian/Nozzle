package com.dluvian.nozzle.ui.components.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.data.utils.replaceWithSuggestion
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.components.postCard.AnnotatedMentionedPostCard
import com.dluvian.nozzle.ui.components.rows.ItemRow
import com.dluvian.nozzle.ui.components.rows.PictureAndName
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun InputBox(
    input: MutableState<TextFieldValue>,
    pubkey: Pubkey,
    picture: String?,
    showProfilePicture: Boolean,
    placeholder: String,
    searchSuggestions: List<SimpleProfile>,
    onSearch: (String) -> Unit,
    onClickMention: (Pubkey) -> Unit,
    postToQuote: AnnotatedMentionedPost? = null,
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
    Column(modifier = Modifier.fillMaxSize(), Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(0.6f, fill = false)) {
            BaseInputBox(
                input = input,
                pubkey = pubkey,
                picture = picture,
                showProfilePicture = showProfilePicture,
                placeholder = placeholder,
            )
            postToQuote?.let { quote ->
                AnnotatedMentionedPostCard(
                    modifier = Modifier.padding(spacing.screenEdge),
                    post = quote,
                    showProfilePicture = showProfilePicture,
                    maxLines = 4,
                    onNavigateToId = { /* Do nothing. Stay in PostScreen */ },
                )
            }
        }
        if (showSuggestions.value && searchSuggestions.isNotEmpty()) {
            SearchSuggestions(
                modifier = Modifier.weight(0.4f),
                suggestions = searchSuggestions,
                showProfilePicture = showProfilePicture,
                onReplaceSuggestion = { profile ->
                    input.value = input.value.replaceWithSuggestion(pubkey = profile.pubkey)
                    onClickMention(profile.pubkey)
                }
            )
        }
    }
}

@Composable
private fun SearchSuggestions(
    suggestions: List<SimpleProfile>,
    showProfilePicture: Boolean,
    onReplaceSuggestion: (SimpleProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        items(items = suggestions) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ItemRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.medium, horizontal = spacing.screenEdge),
                    content = {
                        PictureAndName(
                            profile = it,
                            showProfilePicture = showProfilePicture,
                            onNavigateToProfile = { })
                    },
                    onClick = { onReplaceSuggestion(it) },
                )
            }
        }
    }
}

@Composable
private fun BaseInputBox(
    input: MutableState<TextFieldValue>,
    pubkey: String,
    picture: String?,
    showProfilePicture: Boolean,
    placeholder: String,
) {
    val focusRequester = remember { FocusRequester() }
    Row(modifier = Modifier.fillMaxWidth()) {
        ProfilePicture(
            modifier = Modifier
                .padding(start = spacing.screenEdge, top = spacing.large)
                .padding(top = spacing.small)
                .size(sizing.profilePicture),
            pubkey = pubkey,
            picture = picture,
            showProfilePicture = showProfilePicture,
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
            isTransparent = true
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
