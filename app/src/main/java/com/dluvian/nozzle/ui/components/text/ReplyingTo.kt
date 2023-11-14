package com.dluvian.nozzle.ui.components.text

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.ui.theme.hintGray

@Composable
fun ReplyingTo(name: String, replyRelayHint: String?, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            // TODO: Refactor: Move styles to different file
            withStyle(style = SpanStyle(color = MaterialTheme.colors.hintGray)) {
                if (name.isNotEmpty()) append(stringResource(id = R.string.replying_to))
                else append(stringResource(id = R.string.replying))
            }
            if (name.isNotEmpty()) {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.hintGray
                    )
                ) {
                    append(" ")
                    append(name)
                }
            }
            replyRelayHint?.let { relayHint ->
                withStyle(style = SpanStyle(color = MaterialTheme.colors.hintGray)) {
                    append(" @ ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.hintGray
                    )
                ) {
                    append(relayHint.removeWebsocketPrefix().removeTrailingSlashes())
                }
            }
        },
        // TODO: Don't we have a NozzleText that has overflow and maxLines set which we could use?
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}
