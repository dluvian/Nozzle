package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.theme.QuoteIcon

@Composable
fun QuoteIconButton(
    onQuote: () -> Unit,
    description: String,
    modifier: Modifier = Modifier
) {
    IconButton(modifier = modifier, onClick = onQuote) {
        Icon(imageVector = QuoteIcon, contentDescription = description)
    }
}
