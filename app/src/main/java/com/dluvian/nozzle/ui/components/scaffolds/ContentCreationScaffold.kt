package com.dluvian.nozzle.ui.components.scaffolds

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.relay.RelaySelection
import com.dluvian.nozzle.ui.components.bars.ContentCreationTopBar
import com.dluvian.nozzle.ui.components.dropdown.RelayDropdown
import com.dluvian.nozzle.ui.components.input.InputBox
import com.dluvian.nozzle.ui.components.text.ReplyingTo
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun ContentCreationScaffold(
    input: MutableState<TextFieldValue>,
    relaySelection: List<RelaySelection>,
    searchSuggestions: List<SimpleProfile>,
    postToQuote: AnnotatedMentionedPost?,
    replyingTo: String?,
    pubkey: Pubkey,
    picture: String?,
    showProfilePicture: Boolean,
    toast: String,
    placeholder: String,
    onSearch: (String) -> Unit,
    onClickMention: (Pubkey) -> Unit,
    onSend: (String) -> Unit,
    onGoBack: () -> Unit,
    onToggleRelaySelection: (Int) -> Unit,
) {
    val context = LocalContext.current
    val showRelays = remember { mutableStateOf(false) }
    val isSendable = remember(input.value.text, postToQuote) {
        input.value.text.isNotBlank() || postToQuote != null
    }
    Scaffold(topBar = {
        ContentCreationTopBar(
            isSendable = isSendable,
            onShowRelays = { showRelays.value = true },
            onSend = {
                onSend(input.value.text)
                onGoBack()
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
            },
            onClose = onGoBack
        )
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                RelayDropdown(
                    showMenu = showRelays.value,
                    relays = relaySelection,
                    isEnabled = true,
                    onDismiss = { showRelays.value = false },
                    onToggleIndex = onToggleRelaySelection
                )
            }
            Column(modifier = Modifier.fillMaxSize()) {
                replyingTo?.let { recipient ->
                    ReplyingTo(
                        modifier = Modifier.padding(horizontal = spacing.screenEdge),
                        name = recipient,
                        replyRelayHint = null
                    )
                }
                InputBox(
                    input = input,
                    pubkey = pubkey,
                    picture = picture,
                    showProfilePicture = showProfilePicture,
                    placeholder = placeholder,
                    postToQuote = postToQuote,
                    searchSuggestions = searchSuggestions,
                    onSearch = onSearch,
                    onClickMention = onClickMention
                )
            }
        }
    }
}
