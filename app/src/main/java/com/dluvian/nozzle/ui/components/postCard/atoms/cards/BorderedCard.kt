package com.dluvian.nozzle.ui.components.postCard.atoms.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.HintGray
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun BorderedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        border = BorderStroke(width = spacing.tiny, color = HintGray)
    ) {
        content()
    }
}
