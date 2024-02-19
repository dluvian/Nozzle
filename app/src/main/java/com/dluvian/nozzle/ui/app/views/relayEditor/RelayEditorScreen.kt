package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.UrlUtils.WEBSOCKET_PREFIX
import com.dluvian.nozzle.model.ItemWithOnlineStatus
import com.dluvian.nozzle.model.OnlineStatus
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.Waiting
import com.dluvian.nozzle.ui.components.iconButtons.AddIconButton
import com.dluvian.nozzle.ui.components.iconButtons.DeleteIconButton
import com.dluvian.nozzle.ui.components.iconButtons.SaveIconButton
import com.dluvian.nozzle.ui.components.indicators.OnlineStatusIndicator
import com.dluvian.nozzle.ui.components.indicators.TopBarCircleProgressIndicator
import com.dluvian.nozzle.ui.components.input.AddingTextFieldWithButton
import com.dluvian.nozzle.ui.components.interactors.NamedCheckbox
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun RelayEditorScreen(
    uiState: RelayEditorViewModelState,
    onlineStatuses: Map<Relay, OnlineStatus>,
    onSaveRelays: () -> Unit,
    onAddRelay: (String) -> Boolean,
    onDeleteRelay: (Int) -> Unit,
    onToggleRead: (Int) -> Unit,
    onToggleWrite: (Int) -> Unit,
    onUsePopularRelay: (Int) -> Unit,
    onOpenRelayProfile: (Relay) -> Unit,
    onGoBack: () -> Unit,
) {
    ReturnableScaffold(
        topBarText = stringResource(id = R.string.relays),
        onGoBack = onGoBack,
        actions = {
            if (!uiState.isLoading)
                SaveIconButton(
                    onSave = onSaveRelays,
                    description = stringResource(id = R.string.save_relay_list)
                )
            if (uiState.isLoading) {
                TopBarCircleProgressIndicator()
                Spacer(modifier = Modifier.width(spacing.screenEdge))
            }
        }
    ) {
        val myRelays = remember(uiState.myRelays, onlineStatuses) {
            uiState.myRelays.map { nip65Relay ->
                ItemWithOnlineStatus(
                    item = nip65Relay,
                    onlineStatus = onlineStatuses[nip65Relay.url] ?: Waiting
                )
            }
        }
        val popularRelays = remember(uiState.popularRelays, onlineStatuses) {
            uiState.popularRelays.map { relay ->
                ItemWithOnlineStatus(
                    item = relay,
                    onlineStatus = onlineStatuses[relay] ?: Waiting
                )
            }
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
            onUsePopularRelay = onUsePopularRelay,
            onOpenRelayProfile = onOpenRelayProfile,
        )
    }
}

@Composable
private fun ScreenContent(
    myRelays: List<ItemWithOnlineStatus<Nip65Relay>>,
    popularRelays: List<ItemWithOnlineStatus<Relay>>,
    addIsEnabled: Boolean,
    isError: Boolean,
    onAddRelay: (String) -> Boolean,
    onDeleteRelay: (Int) -> Unit,
    onToggleRead: (Int) -> Unit,
    onToggleWrite: (Int) -> Unit,
    onUsePopularRelay: (Int) -> Unit,
    onOpenRelayProfile: (Relay) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screenEdge)
    )
    {
        if (addIsEnabled) item {
            AddRelay(isError = isError, onAddRelay = onAddRelay)
            Spacer(modifier = Modifier.height(spacing.xxl))
        }

        item { SpacedHeaderText(text = stringResource(id = R.string.my_relays)) }
        itemsIndexed(items = myRelays) { index, relay ->
            MyRelayRow(
                nip65RelayWithOnlineStatus = relay,
                isDeletable = myRelays.size > 1,
                onDeleteRelay = { onDeleteRelay(index) },
                onToggleRead = { onToggleRead(index) },
                onToggleWrite = { onToggleWrite(index) },
                onOpenRelayProfile = onOpenRelayProfile,
            )
            if (index != myRelays.size - 1) {
                HorizontalDivider()
            }
        }
        item { Spacer(modifier = Modifier.height(spacing.xxl)) }

        if (popularRelays.isNotEmpty()) {
            item { SpacedHeaderText(text = stringResource(id = R.string.popular_relays)) }
            itemsIndexed(items = popularRelays) { index, relay ->
                PopularRelayRow(
                    relayWithOnlineStatus = relay,
                    isAddable = addIsEnabled && myRelays.none { it.item.url == relay.item },
                    onUseRelay = { onUsePopularRelay(index) },
                    onOpenRelayProfile = onOpenRelayProfile
                )
                if (index != popularRelays.size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SpacedHeaderText(text: String) {
    Column {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(spacing.medium))
    }
}

@Composable
private fun AddRelay(
    isError: Boolean,
    onAddRelay: (String) -> Boolean,
) {
    Column {
        SpacedHeaderText(text = stringResource(id = R.string.add_relay))
        AddingTextFieldWithButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            isError = isError,
            placeholder = WEBSOCKET_PREFIX,
            onAdd = onAddRelay
        )
    }
}

@Composable
private fun PopularRelayRow(
    relayWithOnlineStatus: ItemWithOnlineStatus<Relay>,
    isAddable: Boolean,
    onUseRelay: () -> Unit,
    onOpenRelayProfile: (Relay) -> Unit
) {
    RelayRow(
        relayWithOnlineStatus = relayWithOnlineStatus,
        onOpenRelayProfile = { onOpenRelayProfile(relayWithOnlineStatus.item) }) {
        if (isAddable) AddIconButton(
            modifier = Modifier.size(sizing.mediumItem),
            onAdd = onUseRelay,
            description = stringResource(id = R.string.add_relay)
        )
    }
}

@Composable
private fun MyRelayRow(
    nip65RelayWithOnlineStatus: ItemWithOnlineStatus<Nip65Relay>,
    isDeletable: Boolean,
    onDeleteRelay: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleWrite: () -> Unit,
    onOpenRelayProfile: (Relay) -> Unit
) {
    RelayRow(
        relayWithOnlineStatus = ItemWithOnlineStatus(
            item = nip65RelayWithOnlineStatus.item.url,
            onlineStatus = nip65RelayWithOnlineStatus.onlineStatus
        ),
        onOpenRelayProfile = { onOpenRelayProfile(nip65RelayWithOnlineStatus.item.url) },
        secondRow = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NamedCheckbox(
                    isChecked = nip65RelayWithOnlineStatus.item.isRead,
                    name = stringResource(id = R.string.read),
                    isEnabled = nip65RelayWithOnlineStatus.item.isWrite || !nip65RelayWithOnlineStatus.item.isRead,
                    onClick = onToggleRead
                )
                Spacer(modifier = Modifier.width(spacing.xxl))
                NamedCheckbox(
                    isChecked = nip65RelayWithOnlineStatus.item.isWrite,
                    name = stringResource(id = R.string.write),
                    isEnabled = nip65RelayWithOnlineStatus.item.isRead || !nip65RelayWithOnlineStatus.item.isWrite,
                    onClick = onToggleWrite
                )
            }
        },
        trailingContent = {
            if (isDeletable) DeleteIconButton(
                onDelete = onDeleteRelay,
                description = stringResource(id = R.string.delete_relay)
            )
        }
    )
}

@Composable
private fun RelayRow(
    relayWithOnlineStatus: ItemWithOnlineStatus<Relay>,
    onOpenRelayProfile: () -> Unit,
    secondRow: @Composable () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(weight = 1f, fill = false)) {
            FirstRelayRow(
                relay = relayWithOnlineStatus.item,
                onlineStatus = relayWithOnlineStatus.onlineStatus,
                onOpenRelayProfile = onOpenRelayProfile
            )
            secondRow()
        }
        trailingContent()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FirstRelayRow(relay: Relay, onlineStatus: OnlineStatus, onOpenRelayProfile: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OnlineStatusIndicator(
            modifier = Modifier
                .weight(1f)
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    spacing = MarqueeSpacing.fractionOfContainer(1f / 4f)
                )
                .clickable(onClick = onOpenRelayProfile),
            onlineStatus = onlineStatus,
            text = relay
        )
    }
}
