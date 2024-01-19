package com.dluvian.nozzle.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


data class Sizing(
    /**
     * 3 dp
     */
    val smallProgressIndicatorStroke: Dp = 3.dp,
    /**
     * 16 dp
     */
    val smallItem: Dp = 16.dp,
    /**
     * 24 dp
     */
    val mediumItem: Dp = 24.dp,
    /**
     * 48 dp
     */
    val largeItem: Dp = 48.dp,
    /**
     * 32 dp
     */
    val smallProfilePicture: Dp = 32.dp,
    /**
     * 40 dp
     */
    val profilePicture: Dp = 34.dp,
    /**
     * 60 dp
     */
    val largeProfilePicture: Dp = 60.dp,
    /**
     * 320 dp
     */
    val dialogHeight: Dp = 320.dp,
)

val LocalSizing = compositionLocalOf { Sizing() }

val sizing: Sizing
    @Composable
    @ReadOnlyComposable
    get() = LocalSizing.current
