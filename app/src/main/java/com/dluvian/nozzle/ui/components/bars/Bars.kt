package com.dluvian.nozzle.ui.components.bars

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.ui.components.buttons.ChooseRelayButton
import com.dluvian.nozzle.ui.components.buttons.CloseButton
import com.dluvian.nozzle.ui.components.buttons.GoBackButton
import com.dluvian.nozzle.ui.components.buttons.SendTopBarButton
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun ContentCreationTopBar(
    relayStatuses: List<RelayActive>,
    isSendable: Boolean,
    onToggleRelaySelection: (Int) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit,
) {
    ClosableTopBar(
        onClose = onClose,
        actions = {
            Row {
                ChooseRelayButton(
                    relays = relayStatuses,
                    onClickIndex = onToggleRelaySelection,
                )
                Spacer(modifier = Modifier.width(spacing.large))
                if (isSendable) {
                    SendTopBarButton(
                        onSend = onSend,
                        onGoBack = onClose,
                    )
                }
            }
        }
    )
}

@Composable
fun ReturnableTopBar(
    text: String,
    onGoBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    BaseTopBar(
        text = text,
        navigationButton = { GoBackButton(onGoBack = onGoBack) },
        actions = actions
    )
}

@Composable
private fun ClosableTopBar(onClose: () -> Unit, actions: @Composable RowScope.() -> Unit) {
    BaseTopBar(
        navigationButton = { CloseButton(onGoBack = onClose) },
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseTopBar(
    text: String = "",
    navigationButton: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = text,
                style = typography.headlineMedium,
                color = Color.White,
            )
        },
        navigationIcon = navigationButton,
        actions = actions

    )
}
