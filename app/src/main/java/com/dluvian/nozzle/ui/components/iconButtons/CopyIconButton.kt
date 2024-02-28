package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.CopyIcon
import com.dluvian.nozzle.ui.theme.sizing


@Composable
fun CopyIconButton(onCopy: () -> Unit, description: String) {
    BaseIconButton(
        modifier = Modifier.size(sizing.largeItem),
        iconModifier = Modifier.size(sizing.smallItem),
        imageVector = CopyIcon,
        description = description,
        onClick = onCopy
    )
}
