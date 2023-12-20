package com.dluvian.nozzle.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun RelaysDialog(
    seenInRelays: List<Relay>,
    onCloseDialog: () -> Unit,
    writesInRelays: List<Relay>? = null,
    readsInRelays: List<Relay>? = null,
) {
    NozzleDialog(onCloseDialog = onCloseDialog) {
        LazyColumn {
            item {
                DialogSection(header = stringResource(id = R.string.seen_in), relays = seenInRelays)
            }
            writesInRelays?.let {
                item {
                    DialogSection(
                        header = stringResource(id = R.string.writes_in),
                        relays = writesInRelays
                    )
                }
            }
            readsInRelays?.let {
                item {
                    DialogSection(
                        header = stringResource(id = R.string.reads_in),
                        relays = readsInRelays
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogSection(header: String, relays: List<String>) {
    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.dialogEdge)
                .padding(top = spacing.large, bottom = spacing.medium),
            text = header + " (${relays.size})",
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        RelayList(relays = relays)
    }
}

@Composable
private fun RelayList(relays: List<String>) {
    Column {
        for (relay in relays) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.dialogEdge, vertical = spacing.medium),
                text = relay,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(spacing.large))
    }
}
