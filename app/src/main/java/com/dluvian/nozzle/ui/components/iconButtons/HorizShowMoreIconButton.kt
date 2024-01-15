package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.components.icons.HorizMoreIcon


@Composable
fun HorizShowMoreIconButton(onShowMore: () -> Unit, description: String) {
    IconButton(onClick = onShowMore) {
        HorizMoreIcon(description = description)
    }
}
