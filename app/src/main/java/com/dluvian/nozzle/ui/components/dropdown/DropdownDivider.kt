package com.dluvian.nozzle.ui.components.dropdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun DropdownDivider() {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.medium)
    )
}
