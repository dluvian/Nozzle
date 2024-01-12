package com.dluvian.nozzle.ui.components.dropdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun DropdownDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.medium)
    )
}
