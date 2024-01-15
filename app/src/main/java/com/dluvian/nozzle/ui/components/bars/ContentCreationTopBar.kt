package com.dluvian.nozzle.ui.components.bars

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.ui.components.buttons.ChooseRelayButton
import com.dluvian.nozzle.ui.components.buttons.SendTopBarButton
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun ContentCreationTopBar(
    isSendable: Boolean,
    relays: List<RelayActive>,
    onToggleRelay: (Int) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit
) {
    ClosableTopBar(text = "", onClose = onClose) {
        ChooseRelayButton(
            relays = relays,
            onClickIndex = onToggleRelay,
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
