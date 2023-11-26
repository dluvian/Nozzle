package com.dluvian.nozzle.ui.app.views.reply

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.ContentCreationTopBar
import com.dluvian.nozzle.ui.components.InputBox
import com.dluvian.nozzle.ui.components.text.ReplyingTo
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun ReplyScreen(
    uiState: ReplyViewModelState,
    pubkeyState: String,
    onToggleRelaySelection: (Int) -> Unit,
    onSend: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val context = LocalContext.current
    val input = remember { mutableStateOf(TextFieldValue()) }
    val isSendable = remember(input.value.text) { input.value.text.isNotBlank() }
    Column(modifier = Modifier.fillMaxSize()) {
        ContentCreationTopBar(
            relayStatuses = uiState.relaySelection,
            isSendable = isSendable,
            onToggleRelaySelection = onToggleRelaySelection,
            onSend = {
                onSend(input.value.text)
                Toast.makeText(
                    context,
                    context.getString(R.string.reply_published),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onClose = onGoBack
        )
        ReplyingTo(
            modifier = Modifier.padding(top = spacing.medium, start = spacing.screenEdge),
            name = uiState.recipientName,
            replyRelayHint = null
        )
        InputBox(
            input = input,
            pubkey = pubkeyState,
            placeholder = stringResource(id = R.string.post_your_reply),
        )
    }
}
