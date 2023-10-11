package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.ui.components.AddingTextFieldWithButton
import com.dluvian.nozzle.ui.components.CheckTopBarButton
import com.dluvian.nozzle.ui.components.DeleteIcon
import com.dluvian.nozzle.ui.components.ExpandAndCollapseIcon
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.text.HeaderText
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun RelayEditorScreen(
    uiState: RelayEditorViewModelState,
    onSaveRelays: () -> Unit,
    onAddRelay: (String) -> Boolean,
    onDeleteRelay: (Int) -> Unit,
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
        ScreenContent(uiState = uiState, onAddRelay = onAddRelay, onDeleteRelay = onDeleteRelay)
    }
}

@Composable
private fun ScreenContent(
    uiState: RelayEditorViewModelState,
    onAddRelay: (String) -> Boolean,
    onDeleteRelay: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(spacing.screenEdge)
            .fillMaxSize()
    ) {
        HeaderText(text = stringResource(id = R.string.add_relay))
        AddingTextFieldWithButton(isError = uiState.isError, onAdd = onAddRelay)
        Spacer(modifier = Modifier.height(spacing.xl))

        HeaderText(text = stringResource(id = R.string.my_relays))
        Spacer(modifier = Modifier.height(spacing.medium))
        RelayItemList(relays = uiState.relays, onDeleteRelay = onDeleteRelay)
    }
}

@Composable
private fun RelayItemList(relays: List<Nip65Relay>, onDeleteRelay: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items = relays) { index, relay ->
            RelayItem(relay = relay, onDeleteRelay = { onDeleteRelay(index) })
            if (index != relays.size - 1) {
                Divider()
            }
        }
    }
}

@Composable
private fun RelayItem(relay: Nip65Relay, onDeleteRelay: () -> Unit) {
    val isExpanded = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        RelayItemHeader(
            url = relay.url,
            isExpanded = isExpanded.value,
            onToggleExpansion = { isExpanded.value = !isExpanded.value },
            onDeleteRelay = onDeleteRelay
        )
        AnimatedVisibility(visible = isExpanded.value) {
            RelayItemExpansion(relay = relay)
        }
    }
}

@Composable
private fun RelayItemHeader(
    url: String,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    onDeleteRelay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.large)
            .clickable(onClick = onToggleExpansion),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            ExpandAndCollapseIcon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onToggleExpansion),
                isExpanded = isExpanded
            )
            Text(
                text = url.removeWebsocketPrefix(),
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DeleteIcon(onDelete = onDeleteRelay)
    }
}

@Composable
private fun RelayItemExpansion(relay: Nip65Relay) {
    Text(text = relay.url)
}
