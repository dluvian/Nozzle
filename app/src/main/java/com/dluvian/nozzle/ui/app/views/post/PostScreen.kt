package com.dluvian.nozzle.ui.app.views.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.ui.components.ContentCreationTopBar
import com.dluvian.nozzle.ui.components.InputBox


@Composable
fun PostScreen(
    uiState: PostViewModelState,
    metadataState: Metadata?,
    onChangeContent: (String) -> Unit,
    onToggleRelaySelection: (Int) -> Unit,
    onSend: () -> Unit,
    onGoBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ContentCreationTopBar(
            relayStatuses = uiState.relayStatuses,
            isSendable = uiState.isSendable,
            onToggleRelaySelection = onToggleRelaySelection,
            onSend = onSend,
            onClose = onGoBack
        )
        InputBox(
            picture = metadataState?.picture.orEmpty(),
            pubkey = uiState.pubkey,
            placeholder = stringResource(id = R.string.post_your_thoughts),
            onChangeInput = onChangeContent
        )
    }
}
