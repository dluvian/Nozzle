package com.dluvian.nozzle.ui.components.postCard.atoms.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun NoParentCard(text: String, onClick: (() -> Unit)? = null) {
    BorderedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenEdge)
            .padding(top = spacing.screenEdge)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.screenEdge),
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}
