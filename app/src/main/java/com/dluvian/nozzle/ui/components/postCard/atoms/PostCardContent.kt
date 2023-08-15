package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.components.text.HyperlinkedText
import com.dluvian.nozzle.ui.components.text.InRelays
import com.dluvian.nozzle.ui.components.text.ReplyingTo
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun PostCardContentBase(
    replyToName: String?,
    replyRelayHint: String?,
    relays: List<String>?,
    content: String,
    isCurrent: Boolean,
    onNavigateToThread: () -> Unit,
) {
    replyToName?.let { ReplyingTo(name = it, replyRelayHint = replyRelayHint) }
    relays?.let { InRelays(relays = it) }
    Spacer(Modifier.height(spacing.medium))
    HyperlinkedText(
        text = content,
        maxLines = if (isCurrent) null else 12,
        onClickNonLink = onNavigateToThread
    )
}