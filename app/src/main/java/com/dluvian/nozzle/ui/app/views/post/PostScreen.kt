package com.dluvian.nozzle.ui.app.views.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.ui.components.ContentCreationTopBar
import com.dluvian.nozzle.ui.components.InputBox
import com.dluvian.nozzle.ui.components.postCard.AnnotatedMentionedPostCard
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun PostScreen(
    uiState: PostViewModelState,
    metadataState: Metadata?,
    pubkeyState: String,
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
        Column(modifier = Modifier.fillMaxSize()) {
            InputBox(
                modifier = Modifier.weight(weight = 1f, fill = false),
                picture = metadataState?.picture.orEmpty(),
                pubkey = pubkeyState,
                placeholder = stringResource(id = R.string.post_your_thoughts),
                onChangeInput = onChangeContent
            )
            uiState.postToQuote?.let { quote ->
                AnnotatedMentionedPostCard(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(spacing.screenEdge),
                    post = quote,
                    onNavigateToId = { /* Do nothing. Stay in PostScreen */ },
                )
            }
        }

    }
}
