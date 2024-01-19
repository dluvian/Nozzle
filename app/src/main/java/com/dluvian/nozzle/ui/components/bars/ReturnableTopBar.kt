package com.dluvian.nozzle.ui.components.bars

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.iconButtons.GoBackIconButton

@Composable
fun ReturnableTopBar(
    text: String,
    onGoBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    BaseTopBar(
        text = text,
        navigationButton = { GoBackIconButton(onGoBack = onGoBack) },
        actions = actions
    )
}
