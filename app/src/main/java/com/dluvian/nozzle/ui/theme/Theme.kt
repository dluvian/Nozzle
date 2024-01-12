package com.dluvian.nozzle.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val lightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Orange700,
)

private val darkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Orange500,
)

@Composable
fun NozzleTheme(isDarkMode: Boolean, content: @Composable () -> Unit) {
    val colors = if (isDarkMode) darkColorPalette else lightColorPalette
    val colors3 = if (isDarkMode) darkColorScheme() else lightColorScheme()

    androidx.compose.material3.MaterialTheme(
        colorScheme = colors3
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
