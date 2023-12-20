package com.dluvian.nozzle.ui.components.text

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.ui.theme.BoldHintGrayStyle
import com.dluvian.nozzle.ui.theme.HintGrayStyle

@Composable
fun ReplyingTo(name: String, replyRelayHint: String?, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(style = HintGrayStyle) {
                if (name.isNotEmpty()) append(stringResource(id = R.string.replying_to))
                else append(stringResource(id = R.string.replying))
            }
            if (name.isNotEmpty()) {
                withStyle(style = BoldHintGrayStyle) {
                    append(" ")
                    append(name)
                }
            }
            replyRelayHint?.let { relayHint ->
                withStyle(style = HintGrayStyle) {
                    append(" @ ")
                }
                withStyle(style = BoldHintGrayStyle) {
                    append(relayHint.removeWebsocketPrefix().removeTrailingSlashes())
                }
            }
        },
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}
