package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
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
    onValidateAndNavigateToDestination: () -> Unit,
    onResetUI: () -> Unit,
    onGoBack: () -> Unit,
) {
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.search),
            onGoBack = onGoBack,
            trailingIcon = {
                SearchTopBarButton(
                    hasChanges = uiState.input.isNotBlank(),
                    onSearch = onValidateAndNavigateToDestination,
                )
            }
        )
        SearchBar(
            input = uiState.input,
            isInvalid = uiState.isInvalid,
            onChangeInput = onChangeInput,
            onNavigateToProfile = onValidateAndNavigateToDestination,
        )
    }
    DisposableEffect(true) {
        onDispose { onResetUI() }
    }
}

@Composable
private fun SearchBar(
    input: String,
    isInvalid: Boolean,
    onChangeInput: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    ChangeableTextField(
        modifier = Modifier.fillMaxWidth(),
        value = input,
        isError = isInvalid,
        maxLines = 2,
        placeholder = stringResource(id = R.string.search_npub_or_note),
        errorLabel = stringResource(id = R.string.invalid_npub_or_note),
        keyboardImeAction = ImeAction.Go,
        onGo = onNavigateToProfile,
        onChangeValue = onChangeInput,
    )
}
