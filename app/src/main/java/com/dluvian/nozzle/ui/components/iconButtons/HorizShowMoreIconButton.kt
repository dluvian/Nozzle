package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.HorizMoreIcon

@Composable
fun HorizShowMoreIconButton(onShowMore: () -> Unit, description: String) {
    IconButton(onClick = onShowMore) {
        Icon(imageVector = HorizMoreIcon, contentDescription = description)
    }
}
