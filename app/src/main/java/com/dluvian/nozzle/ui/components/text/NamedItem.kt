package com.dluvian.nozzle.ui.components.text

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun NamedItem(item: @Composable () -> Unit, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        item()
        Text(text = name)
    }
}
