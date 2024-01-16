package com.dluvian.nozzle.ui.components.dropdown

import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.ui.components.text.NamedItem

@Composable
fun RelayDropdown(
    showMenu: Boolean,
    relays: List<RelayActive>,
    isEnabled: Boolean,
    onDismiss: () -> Unit,
    onToggleIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cleanRelays = remember(relays) {
        relays.map { it.copy(relay = it.relay.removeWebsocketPrefix()) }
    }
    DropdownMenu(
        modifier = modifier,
        expanded = showMenu,
        onDismissRequest = onDismiss
    ) {
        cleanRelays.forEachIndexed { i, relay ->
            DropdownMenuItem(
                text = {
                    NamedItem(
                        item = {
                            Checkbox(
                                checked = relay.isActive,
                                enabled = isEnabled,
                                onCheckedChange = { onToggleIndex(i) })
                        },
                        name = relay.relay
                    )
                },
                onClick = { onToggleIndex(i) },
                enabled = isEnabled,
            )
        }
    }
}
