package com.dluvian.nozzle.ui.components.dropdown

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
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
            DropdownMenuItem(onClick = { }, enabled = false) {
                Text(
                    text = stringResource(id = R.string.no_relays_available),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                text = item.relayUrl.removePrefix("wss://"),
                count = if (isAutopilotUI) item.count else null,
                contentPadding = PaddingValues(start = spacing.medium, end = spacing.xl),
                enabled = !isAutopilotUI,
                onToggle = { onClickIndex(index) }
            )
        }
    }
}
