package com.dluvian.nozzle.data.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dluvian.nozzle.ui.theme.Orange500

@Composable
fun getDefaultPictureBrush(pubkey: String): Brush {
    if (pubkey.isBlank()) return Brush.linearGradient(colors = listOf(Color.Transparent, Orange500))

    val num = pubkey.takeLast(7).toInt(radix = 16)
    val gradient = when (num % 22) {
        0 -> listOf(Color.Transparent, Color(0xFF9d0208))
        1 -> listOf(Color.Transparent, Color(0xFFd00000))
        2 -> listOf(Color.Transparent, Color(0xFFdc2f02))
        3 -> listOf(Color.Transparent, Color(0xFFe85d04))
        4 -> listOf(Color.Transparent, Color(0xFFf48c06))
        5 -> listOf(Color.Transparent, Color(0xFFfaa307))
        6 -> listOf(Color.Transparent, Color(0xFFffc300))
        7 -> listOf(Color.Transparent, Color(0xFFffdd00))
        8 -> listOf(Color.Transparent, Color(0xFFffea00))
        9 -> listOf(Color.Transparent, Color(0xFF7b2cbf))
        10 -> listOf(Color.Transparent, Color(0xFF9d4edd))
        11 -> listOf(Color.Transparent, Color(0xFFc77dff))
        12 -> listOf(Color.Transparent, Color(0xFF008000))
        13 -> listOf(Color.Transparent, Color(0xFF38b000))
        14 -> listOf(Color.Transparent, Color(0xFF70e000))
        15 -> listOf(Color.Transparent, Color(0xFF023e8a))
        16 -> listOf(Color.Transparent, Color(0xFF0096c7))
        17 -> listOf(Color.Transparent, Color(0xFF48cae4))
        18 -> listOf(Color.Transparent, Color(0xFF582f0e))
        19 -> listOf(Color.Transparent, Color(0xFF7f4f24))
        else -> listOf(Color.Transparent, Color(0xFF936639))
    }

    return Brush.linearGradient(colors = gradient)
}