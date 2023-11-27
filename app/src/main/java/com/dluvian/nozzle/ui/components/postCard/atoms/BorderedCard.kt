package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun BorderedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        backgroundColor = backgroundColor,
        border = BorderStroke(width = spacing.tiny, color = Color.LightGray)
    ) {
        content()
    }
}