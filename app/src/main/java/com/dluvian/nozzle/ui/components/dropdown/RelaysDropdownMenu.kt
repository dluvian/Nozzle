package com.dluvian.nozzle.ui.components.dropdown

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun RelaysDropdownMenu(
    showMenu: Boolean,
    menuItems: List<RelayActive>,
    onClickIndex: (Int) -> Unit,
    onDismiss: () -> Unit,
    isAutopilot: Boolean? = null,
    autopilotEnabled: Boolean? = null,
    onToggleAutopilot: (() -> Unit)? = null
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { onDismiss() }
    ) {
        if (menuItems.isEmpty()) {
            SimpleDropdownMenuItem(
                text = stringResource(id = R.string.no_relays_available),
                onClick = {},
                isEnabled = false
            )
        }
    }
    isAutopilot?.let { isChecked ->
        onToggleAutopilot?.let { onToggle ->
            CheckboxDropdownMenuItem(
                isChecked = isChecked,
                enabled = isChecked || autopilotEnabled == true,
                text = stringResource(id = R.string.autopilot),
                contentPadding = PaddingValues(start = spacing.medium, end = spacing.xl),
                onToggle = onToggle,
            )
            DropdownDivider()
        }
    }
    val isAutopilotUI = isAutopilot == true && onToggleAutopilot != null
    menuItems.forEachIndexed { index, item ->
        CheckboxDropdownMenuItem(
            isChecked = item.isActive,
            text = item.relay.removeWebsocketPrefix(),
            contentPadding = PaddingValues(start = spacing.medium, end = spacing.xl),
            enabled = !isAutopilotUI,
            onToggle = { onClickIndex(index) }
        )
    }
}
