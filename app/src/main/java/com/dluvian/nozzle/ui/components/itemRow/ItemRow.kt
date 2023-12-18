package com.dluvian.nozzle.ui.components.itemRow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun ItemRow(
    content: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {},
) {
    Card(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
            Spacer(modifier = Modifier.width(spacing.large))
            trailingContent()
        }
    }
}
