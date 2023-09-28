package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.ChangeableTextField
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.SearchTopBarButton


@Composable
fun SearchScreen(
    uiState: SearchViewModelState,
    onChangeInput: (String) -> Unit,
    onSearch: () -> Unit,
    onNavigateToId: (String) -> Unit,
    onResetUI: () -> Unit,
    onGoBack: () -> Unit,
) {
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.search),
            onGoBack = onGoBack,
            trailingIcon = {
                if (uiState.isLoading) CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .aspectRatio(1f),
                    color = Color.White
                )
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
            onChangeInput = onChangeInput,
            onSearch = onSearch
        )
    }

    val finalIdIsNotEmpty = remember(uiState.finalId) { uiState.finalId.isNotEmpty() }
    if (finalIdIsNotEmpty) onNavigateToId(uiState.finalId)

    DisposableEffect(true) {
        onDispose { onResetUI() }
    }
}

@Composable
private fun SearchBar(
    input: String,
    isInvalidNostrId: Boolean,
    isInvalidNip05: Boolean,
    onChangeInput: (String) -> Unit,
    onSearch: () -> Unit,
) {
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        value = input,
        isError = isInvalidNostrId || isInvalidNip05,
        maxLines = Int.MAX_VALUE,
        placeholder = stringResource(id = R.string.search_nostr_id),
        errorLabel = if (isInvalidNostrId) stringResource(id = R.string.invalid_nostr_id)
        else stringResource(id = R.string.failed_to_resolve_nip05),
        keyboardImeAction = ImeAction.Search,
        onImeAction = onSearch,
        onChangeValue = onChangeInput,
    )
}
