package com.dluvian.nozzle.ui.app.views.reply

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.ui.components.ContentCreationTopBar
import com.dluvian.nozzle.ui.components.InputBox
import com.dluvian.nozzle.ui.components.text.ReplyingTo
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun ReplyScreen(
    uiState: ReplyViewModelState,
    metadataState: Metadata?,
    onChangeReply: (String) -> Unit,
    onToggleRelaySelection: (Int) -> Unit,
    onSend: () -> Unit,
    onGoBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ContentCreationTopBar(
            relayStatuses = uiState.relaySelection,
            isSendable = uiState.isSendable,
            onToggleRelaySelection = onToggleRelaySelection,
            onSend = onSend,
            onClose = onGoBack
        )
        ReplyingTo(
            modifier = Modifier.padding(top = spacing.medium, start = spacing.screenEdge),
            name = uiState.recipientName,
            replyRelayHint = null
        )
        InputBox(
            picture = metadataState?.picture.orEmpty(),
            pubkey = uiState.pubkey,
            placeholder = stringResource(id = R.string.post_your_reply),
            onChangeInput = onChangeReply
        )
    }
}
