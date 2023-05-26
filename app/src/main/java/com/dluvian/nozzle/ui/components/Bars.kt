package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.ui.theme.White21
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
        trailingIcon = {
            Row {
                ChooseRelayButton(
                    relays = relayStatuses,
                    onClickIndex = onToggleRelaySelection,
                )
                Spacer(modifier = Modifier.width(spacing.large))
                SendTopBarButton(
                    isSendable = isSendable,
                    onSend = onSend,
                    onGoBack = onClose,
                )
            }
        }
    )
}

@Composable
fun ReturnableTopBar(
    text: String,
    onGoBack: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    BaseTopBar(
        text = text,
        leadingIcon = { GoBackButton(onGoBack = onGoBack) },
        trailingIcon = trailingIcon
    )
}

@Composable
private fun ClosableTopBar(onClose: () -> Unit, trailingIcon: @Composable (() -> Unit)? = null) {
    BaseTopBar(
        leadingIcon = { CloseButton(onGoBack = onClose) },
        trailingIcon = trailingIcon
    )
}

@Composable
private fun BaseTopBar(
    text: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    TopAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    leadingIcon()
                }
                if (text != null) {
                    Spacer(modifier = Modifier.width(spacing.large))
                    Text(
                        text = text,
                        style = typography.h6,
                        color = White21,
                    )
                }
            }
            if (trailingIcon != null) {
                trailingIcon()
            }

        }
    }
}
