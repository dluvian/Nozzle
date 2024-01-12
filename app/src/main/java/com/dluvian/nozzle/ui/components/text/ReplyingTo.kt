package com.dluvian.nozzle.ui.components.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix

@Composable
fun ReplyingTo(name: String, replyRelayHint: String?, modifier: Modifier = Modifier) {
    // TODO: Add gray again
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
                if (name.isNotEmpty()) append(stringResource(id = R.string.replying_to))
                else append(stringResource(id = R.string.replying))
            if (name.isNotEmpty()) {
                    append(" ")
                    append(name)
                }
            replyRelayHint?.let { relayHint ->
                    append(" @ ")
                    append(relayHint.removeWebsocketPrefix().removeTrailingSlashes())
            }
        },
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}
