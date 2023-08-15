package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun BorderedCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.border(
            width = spacing.tiny,
            color = Color.LightGray,
            shape = RoundedCornerShape(spacing.large)
        )
    ) {
        content()
    }
}