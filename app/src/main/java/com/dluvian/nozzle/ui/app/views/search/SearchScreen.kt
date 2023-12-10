package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.ui.components.ChangeableTextField
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.SearchTopBarButton
import com.dluvian.nozzle.ui.components.TopBarCircleProgressIndicator
import com.dluvian.nozzle.ui.components.itemRow.ItemRow
import com.dluvian.nozzle.ui.components.itemRow.PictureAndName


@Composable
fun SearchScreen(
    uiState: SearchViewModelState,
    profileSearchResult: List<ProfileSearchResult>,
    onSearch: (String) -> Unit,
    onResetUI: () -> Unit,
    onNavigateToId: (String) -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit,
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
            isInvalidNip05 = uiState.isInvalidNip05,
            focusRequester = focusRequester,
            onSearch = { onSearch(input.value.text) }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(profileSearchResult) {
                ItemRow(content = {
                    PictureAndName(
                        profile = it.profile,
                        onNavigateToProfile = onNavigateToProfile
                    )
                }, onClick = { onNavigateToProfile(it.profile.pubkey) })
            }
        }
        val finalIdIsNotEmpty = remember(uiState.finalId) { uiState.finalId.isNotEmpty() }
        if (finalIdIsNotEmpty) onNavigateToId(uiState.finalId)
    }

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
    isInvalidNip05: Boolean,
    focusRequester: FocusRequester,
    onSearch: () -> Unit,
) {
    ChangeableTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        isError = isInvalidNip05,
        input = input,
        maxLines = Int.MAX_VALUE,
        placeholder = stringResource(id = R.string.search_profiles),
        errorLabel = if (isInvalidNip05) stringResource(id = R.string.failed_to_resolve_nip05)
        else null,
        keyboardImeAction = ImeAction.Search,
        onImeAction = onSearch,
    )
}
