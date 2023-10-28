package com.dluvian.nozzle.ui.app.views.post

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
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
    onToggleRelaySelection: (Int) -> Unit,
    onSend: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val context = LocalContext.current
    val input = remember { mutableStateOf(TextFieldValue()) }
    val isSendable = remember(input.value.text, uiState.postToQuote) {
        input.value.text.isNotBlank() || uiState.postToQuote != null
    }
    Column(modifier = Modifier.fillMaxSize()) {
        ContentCreationTopBar(
            relayStatuses = uiState.relayStatuses,
            isSendable = isSendable,
            onToggleRelaySelection = onToggleRelaySelection,
            onSend = {
                onSend(input.value.text)
                Toast.makeText(
                    context,
                    context.getString(R.string.post_published),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onClose = onGoBack
        )
        Column(modifier = Modifier.fillMaxSize()) {
            InputBox(
                modifier = Modifier.weight(weight = 1f, fill = false),
                input = input,
                picture = metadataState?.picture.orEmpty(),
                pubkey = pubkeyState,
                placeholder = stringResource(id = R.string.post_your_thoughts),
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
