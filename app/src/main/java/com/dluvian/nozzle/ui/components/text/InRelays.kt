package com.dluvian.nozzle.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.ui.components.dialog.RelaysDialog
import com.dluvian.nozzle.ui.theme.BoldHintGrayStyle
import com.dluvian.nozzle.ui.theme.HintGrayStyle
import com.dluvian.nozzle.ui.theme.Shapes

@Composable
fun InRelays(relays: List<Relay>, onOpenRelayProfile: (Relay) -> Unit) {
    val openDialog = remember { mutableStateOf(false) }
    if (openDialog.value) {
        RelaysDialog(
            seenInRelays = relays,
            onOpenRelayProfile = onOpenRelayProfile,
            onCloseDialog = { openDialog.value = false })
    }
    if (relays.isNotEmpty()) {
        Row(
            modifier = Modifier
                .clip(Shapes.small)
                .clickable { openDialog.value = true }) {
            InRelay(
                modifier = Modifier.weight(weight = 0.65f, fill = false), relay = relays.first()
            )
            if (relays.size > 1) {
                AndOthers(
                    modifier = Modifier.weight(weight = 0.35f, fill = false),
                    otherRelaysCount = relays.size - 1
                )
            }
        }
    }
}

@Composable
private fun InRelay(
    modifier: Modifier = Modifier,
    relay: String,
) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(HintGrayStyle) {
                append(stringResource(id = R.string.in_relay))
                append(" ")
            }
            withStyle(style = BoldHintGrayStyle) {
                append(relay.removeWebsocketPrefix())
            }
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun AndOthers(
    otherRelaysCount: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier, text = buildAnnotatedString {
            withStyle(style = HintGrayStyle) {
                append(" ")
                append(stringResource(id = R.string.and))
                append(" ")
            }
            withStyle(style = BoldHintGrayStyle) {
                append(otherRelaysCount.toString())
                append(" ")
                append(pluralStringResource(id = R.plurals.other_relays, otherRelaysCount))
            }
        }, maxLines = 1, overflow = TextOverflow.Ellipsis
    )
}
