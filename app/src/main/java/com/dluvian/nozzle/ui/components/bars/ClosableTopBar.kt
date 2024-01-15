package com.dluvian.nozzle.ui.components.bars

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.iconButtons.CloseIconButton

@Composable
fun ClosableTopBar(
    text: String,
    onClose: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    BaseTopBar(
        text = text,
        navigationButton = { CloseIconButton(onClose = onClose) },
        actions = actions
    )
}
