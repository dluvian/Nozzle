package com.dluvian.nozzle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val lightColorPalette = lightColorScheme(
)

private val darkColorPalette = darkColorScheme(
)

@Composable
fun NozzleTheme(isDarkMode: Boolean, content: @Composable () -> Unit) {
    val colors = if (isDarkMode) darkColorPalette else lightColorPalette

    MaterialTheme(colorScheme = colors) {
        content()
    }
}
