package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.AddingTextFieldWithButton
import com.dluvian.nozzle.ui.components.CheckTopBarButton
import com.dluvian.nozzle.ui.components.ReturnableTopBar

@Composable
fun RelayEditorScreen(
    uiState: RelayEditorViewModelState,
    onSaveRelays: () -> Unit,
    onAddRelay: (String) -> Boolean,
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
        Text(text = stringResource(id = R.string.my_relays))
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            for (relay in uiState.relays) {
                Text(text = "${relay.url} r:${relay.isRead} w:${relay.isWrite}")
            }
        }
        Text(text = stringResource(id = R.string.add_relay))
        AddingTextFieldWithButton(isError = uiState.isError, onAdd = onAddRelay)
    }
}
