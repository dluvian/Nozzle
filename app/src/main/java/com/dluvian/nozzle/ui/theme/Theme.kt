package com.dluvian.nozzle.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val lightColorPalette = lightColorScheme(
    primary = Color(0xFF6d23f9),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFFe8ddff),
    onPrimaryContainer = Color(0xFF22005d),
    secondary = Color(0xFF8b5000),
    onSecondary = Color(0xFFffffff),
    secondaryContainer = Color(0xFFffdcbe),
    onSecondaryContainer = Color(0xFF2c1600),
)

private val darkColorPalette = darkColorScheme(
    primary = Color(0xFFcfbdff),
    onPrimary = Color(0xFF3a0093),
    primaryContainer = Color(0xFF5300cd),
    onPrimaryContainer = Color(0xFFe8ddff),
    secondary = Color(0xFFffb870),
    onSecondary = Color(0xFF4a2800),
    secondaryContainer = Color(0xFF693c00),
    onSecondaryContainer = Color(0xFFffdcbe),
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color.Black,
)

@Composable
fun NozzleTheme(isDarkMode: Boolean, content: @Composable () -> Unit) {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors = when {
        dynamicColor && isDarkMode -> dynamicDarkColorScheme(LocalContext.current).copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color.Black,
        )

        dynamicColor && !isDarkMode -> dynamicLightColorScheme(LocalContext.current)
        isDarkMode -> darkColorPalette
        else -> lightColorPalette
    }

    MaterialTheme(colorScheme = colors) {
        content()
    }
}
