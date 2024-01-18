package com.dluvian.nozzle.ui.app.views.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.ui.components.scaffolds.ContentCreationScaffold


@Composable
fun PostScreen(
    uiState: PostViewModelState,
    pubkey: Pubkey,
    onToggleRelaySelection: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onClickMention: (Pubkey) -> Unit,
    onSend: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val input = remember { mutableStateOf(TextFieldValue()) }
    ContentCreationScaffold(
        input = input,
        relaySelection = uiState.relaySelection,
        searchSuggestions = uiState.searchSuggestions,
        postToQuote = uiState.postToQuote,
        replyingTo = null,
        pubkey = pubkey,
        toast = stringResource(id = R.string.post_published),
        placeholder = stringResource(id = R.string.post_your_thoughts),
        onSearch = onSearch,
        onClickMention = onClickMention,
        onSend = onSend,
        onGoBack = onGoBack,
        onToggleRelaySelection = onToggleRelaySelection
    )
}
