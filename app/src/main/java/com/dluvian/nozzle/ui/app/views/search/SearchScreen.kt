package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.ChangeableTextField
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.SearchTopBarButton
import com.dluvian.nozzle.ui.components.TopBarCircleProgressIndicator


@Composable
fun SearchScreen(
    uiState: SearchViewModelState,
    onSearch: (String) -> Unit,
    onNavigateToId: (String) -> Unit,
    onResetUI: () -> Unit,
    onGoBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val input = remember { mutableStateOf(TextFieldValue()) }
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.search),
            onGoBack = onGoBack,
            trailingIcon = {
                if (uiState.isLoading) TopBarCircleProgressIndicator()
                else SearchTopBarButton(
                    hasChanges = input.value.text.isNotBlank(),
                    onSearch = { onSearch(input.value.text) },
                )
            }
        )
        SearchBar(
            input = input,
            isInvalidNostrId = uiState.isInvalidNostrId,
            isInvalidNip05 = uiState.isInvalidNip05,
            focusRequester = focusRequester,
            onSearch = { onSearch(input.value.text) }
        )
    }

    val finalIdIsNotEmpty = remember(uiState.finalId) { uiState.finalId.isNotEmpty() }
    if (finalIdIsNotEmpty) onNavigateToId(uiState.finalId)

    DisposableEffect(true) {
        onDispose { onResetUI() }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun SearchBar(
    input: MutableState<TextFieldValue>,
    isInvalidNostrId: Boolean,
    isInvalidNip05: Boolean,
    focusRequester: FocusRequester,
    onSearch: () -> Unit,
) {
    ChangeableTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        isError = isInvalidNostrId || isInvalidNip05,
        input = input,
        maxLines = Int.MAX_VALUE,
        placeholder = stringResource(id = R.string.open_nostr_id),
        errorLabel = if (isInvalidNostrId) stringResource(id = R.string.invalid_nostr_id)
        else stringResource(id = R.string.failed_to_resolve_nip05),
        keyboardImeAction = ImeAction.Search,
        onImeAction = onSearch,
    )
}
