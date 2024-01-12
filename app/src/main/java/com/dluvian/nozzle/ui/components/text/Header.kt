package com.dluvian.nozzle.ui.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HeaderText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
    )
}
