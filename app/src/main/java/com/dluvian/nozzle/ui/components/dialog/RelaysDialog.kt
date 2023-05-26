package com.dluvian.nozzle.ui.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.ui.theme.spacing
import com.dluvian.nozzle.R

@Composable
fun RelaysDialog(relays: List<String>, onCloseDialog: () -> Unit) {
    NozzleDialog(onCloseDialog = onCloseDialog) {
        Column {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.dialogEdge)
                    .padding(top = spacing.large, bottom = spacing.medium),
                text = stringResource(id = R.string.relays),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            RelayList(relays = relays)
        }
    }
}

@Composable
private fun RelayList(relays: List<String>) {
    LazyColumn {
        items(relays) { relay ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.dialogEdge, vertical = spacing.medium),
                text = relay,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        item { Spacer(modifier = Modifier.height(spacing.large)) }
    }
}
