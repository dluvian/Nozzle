package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun BorderedCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        border = BorderStroke(width = spacing.tiny, color = Color.LightGray)
    ) {
        content()
    }
}