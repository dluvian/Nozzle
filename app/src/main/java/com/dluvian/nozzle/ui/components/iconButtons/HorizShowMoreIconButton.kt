package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.HorizMoreIcon

@Composable
fun HorizShowMoreIconButton(onShowMore: () -> Unit, description: String) {
    BaseIconButton(imageVector = HorizMoreIcon, description = description, onClick = onShowMore)
}
