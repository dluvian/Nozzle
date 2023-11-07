package com.dluvian.nozzle.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Orange700,
    onPrimary = Color.White,
    onSecondary = Color.White,
)

private val darkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Orange500,
    onPrimary = Color.White,
    onSecondary = Color.White,
)

@Composable
fun NozzleTheme(isDarkMode: Boolean, content: @Composable () -> Unit) {
    val colors = if (isDarkMode) darkColorPalette else lightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
