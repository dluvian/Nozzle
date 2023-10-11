package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.ui.components.AddingTextFieldWithButton
import com.dluvian.nozzle.ui.components.CheckTopBarButton
import com.dluvian.nozzle.ui.components.DeleteIcon
import com.dluvian.nozzle.ui.components.NamedCheckbox
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.text.HeaderText
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun RelayEditorScreen(
    uiState: RelayEditorViewModelState,
    onSaveRelays: () -> Unit,
    onAddRelay: (String) -> Boolean,
    onDeleteRelay: (Int) -> Unit,
    onToggleRead: (Int) -> Unit,
    onToggleWrite: (Int) -> Unit,
    onGoBack: () -> Unit,
) {
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.relays),
            onGoBack = onGoBack,
            trailingIcon = {
                CheckTopBarButton(
                    hasChanges = uiState.hasChanges,
                    onCheck = onSaveRelays,
                )
            })
        ScreenContent(
            uiState = uiState,
            onAddRelay = onAddRelay,
            onDeleteRelay = onDeleteRelay,
            onToggleRead = onToggleRead,
            onToggleWrite = onToggleWrite
        )
    }
}

@Composable
private fun ScreenContent(
    uiState: RelayEditorViewModelState,
    onAddRelay: (String) -> Boolean,
    onDeleteRelay: (Int) -> Unit,
    onToggleRead: (Int) -> Unit,
    onToggleWrite: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(spacing.screenEdge)
            .fillMaxSize()
    ) {
        HeaderText(text = stringResource(id = R.string.add_relay))
        AddingTextFieldWithButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            isError = uiState.isError,
            onAdd = onAddRelay
        )
        Spacer(modifier = Modifier.height(spacing.xl))

        HeaderText(text = stringResource(id = R.string.my_relays))
        Spacer(modifier = Modifier.height(spacing.medium))
        RelayItemList(
            relays = uiState.relays,
            onDeleteRelay = onDeleteRelay,
            onToggleRead = onToggleRead,
            onToggleWrite = onToggleWrite
        )
    }
}

@Composable
private fun RelayItemList(
    relays: List<Nip65Relay>,
    onDeleteRelay: (Int) -> Unit,
    onToggleRead: (Int) -> Unit,
    onToggleWrite: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items = relays) { index, relay ->
            RelayItem(relay = relay,
                onDeleteRelay = { onDeleteRelay(index) },
                onToggleRead = { onToggleRead(index) },
                onToggleWrite = { onToggleWrite(index) }
            )
            if (index != relays.size - 1) {
                Divider()
            }
        }
    }
}

@Composable
private fun RelayItem(
    relay: Nip65Relay,
    onDeleteRelay: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleWrite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.large),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = relay.url.removeWebsocketPrefix(),
            color = DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row {
            Spacer(modifier = Modifier.width(spacing.large))
            NamedCheckbox(
                isChecked = relay.isRead,
                name = stringResource(id = R.string.read),
                isEnabled = relay.isWrite || !relay.isRead,
                textColor = DarkGray,
                onClick = onToggleRead
            )
            Spacer(modifier = Modifier.width(spacing.xxl))

            NamedCheckbox(
                isChecked = relay.isWrite,
                name = stringResource(id = R.string.write),
                isEnabled = relay.isRead || !relay.isWrite,
                textColor = DarkGray,
                onClick = onToggleWrite
            )
            Spacer(modifier = Modifier.width(spacing.xl))

            DeleteIcon(onDelete = onDeleteRelay)
        }
    }
}
