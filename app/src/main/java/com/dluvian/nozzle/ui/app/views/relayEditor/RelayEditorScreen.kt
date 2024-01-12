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
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.UrlUtils.WEBSOCKET_PREFIX
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.ui.components.bars.ReturnableTopBar
import com.dluvian.nozzle.ui.components.icons.AddIcon
import com.dluvian.nozzle.ui.components.icons.DeleteIcon
import com.dluvian.nozzle.ui.components.icons.SaveIcon
import com.dluvian.nozzle.ui.components.indicators.TopBarCircleProgressIndicator
import com.dluvian.nozzle.ui.components.input.AddingTextFieldWithButton
import com.dluvian.nozzle.ui.components.namedItem.NamedCheckbox
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
    onUsePopularRelay: (Int) -> Unit,
    onGoBack: () -> Unit,
) {
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.relays),
            onGoBack = onGoBack,
            trailingIcon = {
                if (uiState.hasChanges && !uiState.isLoading) SaveIcon(onSave = onSaveRelays)
                if (uiState.isLoading) TopBarCircleProgressIndicator()
            })
        val myRelays = remember(uiState.myRelays) {
            uiState.myRelays.map { relay -> relay.copy(url = relay.url.removeWebsocketPrefix()) }
        }
        val popularRelays = remember(uiState.popularRelays) {
            uiState.popularRelays.map { it.removeWebsocketPrefix() }
        }
        ScreenContent(
            myRelays = myRelays,
            popularRelays = popularRelays,
            addIsEnabled = uiState.addIsEnabled,
            isError = uiState.isError,
            onAddRelay = onAddRelay,
            onDeleteRelay = onDeleteRelay,
            onToggleRead = onToggleRead,
            onToggleWrite = onToggleWrite,
            onUsePopularRelay = onUsePopularRelay
        )
    }
}

@Composable
private fun ScreenContent(
    myRelays: List<Nip65Relay>,
    popularRelays: List<String>,
    addIsEnabled: Boolean,
    isError: Boolean,
    onAddRelay: (String) -> Boolean,
    onDeleteRelay: (Int) -> Unit,
    onToggleRead: (Int) -> Unit,
    onToggleWrite: (Int) -> Unit,
    onUsePopularRelay: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(spacing.screenEdge)
            .fillMaxSize()
    )
    {
        item { AddRelay(isError = isError, isEnabled = addIsEnabled, onAddRelay = onAddRelay) }
        item { Spacer(modifier = Modifier.height(spacing.xxl)) }

        item { SpacedHeaderText(text = stringResource(id = R.string.my_relays)) }
        itemsIndexed(items = myRelays) { index, relay ->
            MyRelayRow(
                relay = relay,
                isDeletable = myRelays.size > 1,
                onDeleteRelay = { onDeleteRelay(index) },
                onToggleRead = { onToggleRead(index) },
                onToggleWrite = { onToggleWrite(index) }
            )
            if (index != myRelays.size - 1) {
                Divider()
            }
        }
        item { Spacer(modifier = Modifier.height(spacing.xxl)) }


        if (popularRelays.isNotEmpty()) {
            item { SpacedHeaderText(text = stringResource(id = R.string.my_friends_relays)) }
            itemsIndexed(items = popularRelays) { index, relay ->
                PopularRelayRow(
                    relay = relay,
                    isAddable = addIsEnabled && myRelays.none { it.url == relay },
                    onUseRelay = { onUsePopularRelay(index) })
                if (index != popularRelays.size - 1) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun SpacedHeaderText(text: String) {
    Column {
        HeaderText(text = text)
        Spacer(modifier = Modifier.height(spacing.medium))
    }
}

@Composable
private fun AddRelay(
    isError: Boolean,
    isEnabled: Boolean,
    onAddRelay: (String) -> Boolean,
) {
    Column {
        SpacedHeaderText(text = stringResource(id = R.string.add_relay))
        AddingTextFieldWithButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            isEnabled = isEnabled,
            isError = isError,
            placeholder = WEBSOCKET_PREFIX,
            onAdd = onAddRelay
        )
    }
}

@Composable
private fun PopularRelayRow(relay: String, isAddable: Boolean, onUseRelay: () -> Unit) {
    RelayRow(relay = relay) {
        if (isAddable) AddIcon(onAdd = onUseRelay)
    }
}

@Composable
private fun MyRelayRow(
    relay: Nip65Relay,
    isDeletable: Boolean,
    onDeleteRelay: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleWrite: () -> Unit
) {
    RelayRow(relay = relay.url) {
        Row {
            Spacer(modifier = Modifier.width(spacing.large))
            NamedCheckbox(
                isChecked = relay.isRead,
                name = stringResource(id = R.string.read),
                isEnabled = relay.isWrite || !relay.isRead,
                onClick = onToggleRead
            )
            Spacer(modifier = Modifier.width(spacing.xxl))

            NamedCheckbox(
                isChecked = relay.isWrite,
                name = stringResource(id = R.string.write),
                isEnabled = relay.isRead || !relay.isWrite,
                onClick = onToggleWrite
            )
            Spacer(modifier = Modifier.width(spacing.xl))

            if (isDeletable) DeleteIcon(onDelete = onDeleteRelay)
        }
    }
}

@Composable
private fun RelayRow(relay: String, trailingContent: @Composable (() -> Unit)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.large),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = relay,
            color = DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        trailingContent()
    }
}
