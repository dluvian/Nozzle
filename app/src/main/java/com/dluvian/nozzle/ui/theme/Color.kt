package com.dluvian.nozzle.ui.theme

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Orange700 = Color(0xFFF57C00)
val Orange500 = Color(0xFFFF9800)

val HyperlinkBlue = Color(0xFF007AFF)

val Colors.hintGray: Color
    @Composable
    get() = if (isLight) Color.LightGray else Color.DarkGray
