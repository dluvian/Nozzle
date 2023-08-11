package com.dluvian.nozzle.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.dialog.RelaysDialog
import com.dluvian.nozzle.ui.theme.Shapes

@Composable
fun InRelays(relays: List<String>) {
    val openDialog = remember { mutableStateOf(false) }
    if (openDialog.value) {
        RelaysDialog(relays = relays, onCloseDialog = { openDialog.value = false })
    }
    if (relays.isNotEmpty()) {
        Row(modifier = Modifier
            .clip(Shapes.small)
            .let {
                if (relays.size > 1) it.clickable { openDialog.value = true }
                else it
            }
        ) {
            InRelay(
                modifier = Modifier.weight(weight = 0.65f, fill = false),
                relay = relays.first()
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
private fun InRelay(relay: String, modifier: Modifier = Modifier, color: Color = Color.LightGray) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = color)) {
                append(stringResource(id = R.string.in_relay))
                append(" ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                // TODO: Util function for removing ws prefix
                append(relay.removePrefix("wss://"))
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
    color: Color = Color.LightGray
) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = color)) {
                append(" ")
                append(stringResource(id = R.string.and))
                append(" ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                append(otherRelaysCount.toString())
                append(" ")
                append(pluralStringResource(id = R.plurals.other_relays, otherRelaysCount))
            }
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
