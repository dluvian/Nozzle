package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.CheckTopBarButton
import com.dluvian.nozzle.ui.components.ReturnableTopBar

@Composable
fun RelayEditorScreen(
    uiState: RelayEditorViewModelState,
    onSaveRelays: () -> Unit,
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState()
        ) {
            itemsIndexed(items = uiState.relays) { index, relay ->
                Text(text = "${relay.url} r:${relay.isRead} w:${relay.isWrite}")
            }
        }
    }
}
