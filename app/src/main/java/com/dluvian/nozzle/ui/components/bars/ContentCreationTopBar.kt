package com.dluvian.nozzle.ui.components.bars

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.iconButtons.RelayIconButton
import com.dluvian.nozzle.ui.components.iconButtons.SendIconButton

@Composable
fun ContentCreationTopBar(
    isSendable: Boolean,
    onShowRelays: () -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit
) {
    ClosableTopBar(text = "", onClose = onClose) {
        RelayIconButton(
            onClick = onShowRelays,
            description = stringResource(id = R.string.show_relays)
        )
        if (isSendable) {
            SendIconButton(
                onSend = onSend,
                description = stringResource(id = R.string.send)
            )
        }
    }
}
