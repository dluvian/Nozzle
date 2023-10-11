package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.ChangeableTextField
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.SearchTopBarButton
import com.dluvian.nozzle.ui.components.TopBarCircleProgressIndicator


@Composable
fun SearchScreen(
    uiState: SearchViewModelState,
    onChangeInput: (String) -> Unit,
    onSearch: () -> Unit,
    onNavigateToId: (String) -> Unit,
    onResetUI: () -> Unit,
    onGoBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.search),
            onGoBack = onGoBack,
            trailingIcon = {
                if (uiState.isLoading) TopBarCircleProgressIndicator()
                else SearchTopBarButton(
                    hasChanges = uiState.input.isNotBlank(),
                    onSearch = onSearch,
                )
            }
        )
        SearchBar(
            input = uiState.input,
            isInvalidNostrId = uiState.isInvalidNostrId,
            isInvalidNip05 = uiState.isInvalidNip05,
            focusRequester = focusRequester,
            onChangeInput = onChangeInput,
            onSearch = onSearch
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
    input: String,
    isInvalidNostrId: Boolean,
    isInvalidNip05: Boolean,
    focusRequester: FocusRequester,
    onChangeInput: (String) -> Unit,
    onSearch: () -> Unit,
) {
    ChangeableTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        value = input,
        isError = isInvalidNostrId || isInvalidNip05,
        maxLines = Int.MAX_VALUE,
        placeholder = stringResource(id = R.string.open_nostr_id),
        errorLabel = if (isInvalidNostrId) stringResource(id = R.string.invalid_nostr_id)
        else stringResource(id = R.string.failed_to_resolve_nip05),
        keyboardImeAction = ImeAction.Search,
        onImeAction = onSearch,
        onChangeValue = onChangeInput,
    )
}
