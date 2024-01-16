package com.dluvian.nozzle.ui.app.views.post

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.ui.components.bars.ContentCreationTopBar
import com.dluvian.nozzle.ui.components.dropdown.RelayDropdown
import com.dluvian.nozzle.ui.components.input.InputBox


@Composable
fun PostScreen(
    uiState: PostViewModelState,
    pubkeyState: String,
    onToggleRelaySelection: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onClickMention: (Pubkey) -> Unit,
    onSend: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val context = LocalContext.current
    val input = remember { mutableStateOf(TextFieldValue()) }
    val isSendable = remember(input.value.text, uiState.postToQuote) {
        input.value.text.isNotBlank() || uiState.postToQuote != null
    }
    val showRelays = remember { mutableStateOf(false) }
    Scaffold(topBar = {
        ContentCreationTopBar(
            isSendable = isSendable,
            onShowRelays = { showRelays.value = true },
            onSend = {
                onSend(input.value.text)
                onGoBack()
                Toast.makeText(
                    context,
                    context.getString(R.string.post_published),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onClose = onGoBack
        )
    }) {
        Box(modifier = Modifier.padding(it)) {
            RelayDropdown(
                modifier = Modifier.align(Alignment.TopEnd),
                showMenu = showRelays.value,
                relays = uiState.relayStatuses,
                isEnabled = true,
                onDismiss = { showRelays.value = false },
                onToggleIndex = onToggleRelaySelection
            )
            InputBox(
                input = input,
                pubkey = pubkeyState,
                placeholder = stringResource(id = R.string.post_your_thoughts),
                postToQuote = uiState.postToQuote,
                searchSuggestions = uiState.searchSuggestions,
                onSearch = onSearch,
                onClickMention = onClickMention
            )
        }
    }
}
