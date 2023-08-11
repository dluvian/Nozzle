package com.dluvian.nozzle.ui.components.text

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.dluvian.nozzle.R

@Composable
fun ReplyingTo(name: String, replyRelayHint: String?, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            // TODO: Refactor: Move styles to different file
            withStyle(style = SpanStyle(color = Color.LightGray)) {
                append(stringResource(id = R.string.replying_to))
                append(" ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.LightGray)) {
                append(name)
            }
            replyRelayHint?.let { relayHint ->
                withStyle(style = SpanStyle(color = Color.LightGray)) {
                    append(" @ ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                ) {
                    // TODO: Util function for removing ws prefix and trailing slash
                    append(relayHint.removePrefix("wss://").removeSuffix("/"))
                }
            }
        },
        // TODO: Don't we have a NozzleText that has overflow and maxLines set which we could use?
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}
