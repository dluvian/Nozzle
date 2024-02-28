package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.QuoteIcon

@Composable
fun QuoteIconButton(
    onQuote: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) {
    BaseIconButton(
        modifier = modifier,
        iconModifier = iconModifier,
        imageVector = QuoteIcon,
        description = description,
        onClick = onQuote
    )
}
