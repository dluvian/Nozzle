package com.dluvian.nozzle.ui.app.views.reply

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.dluvian.nozzle.ui.components.text.ReplyingTo


@Composable
fun ReplyScreen(
    uiState: ReplyViewModelState,
    pubkeyState: String,
    onToggleRelaySelection: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onClickMention: (Pubkey) -> Unit,
    onSend: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val context = LocalContext.current
    val input = remember { mutableStateOf(TextFieldValue()) }
    val isSendable = remember(input.value.text) { input.value.text.isNotBlank() }
    val showRelays = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            ContentCreationTopBar(
                isSendable = isSendable,
                onShowRelays = { showRelays.value = true },
                onSend = {
                    onSend(input.value.text)
                    onGoBack()
                    Toast.makeText(
                        context,
                        context.getString(R.string.reply_published),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onClose = onGoBack
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            RelayDropdown(
                modifier = Modifier.align(Alignment.TopEnd),
                showMenu = showRelays.value,
                relays = uiState.relaySelection,
                isEnabled = true,
                onDismiss = { showRelays.value = false },
                onToggleIndex = onToggleRelaySelection
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                ReplyingTo(
                    name = uiState.recipientName,
                    replyRelayHint = null
                )
                InputBox(
                    input = input,
                    pubkey = pubkeyState,
                    placeholder = stringResource(id = R.string.post_your_reply),
                    searchSuggestions = uiState.searchSuggestions,
                    onSearch = onSearch,
                    onClickMention = onClickMention
                )
            }
        }
    }
}
