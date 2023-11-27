package com.dluvian.nozzle.data.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.dluvian.nozzle.ui.theme.ProfilePictureApricot
import com.dluvian.nozzle.ui.theme.ProfilePictureBeige
import com.dluvian.nozzle.ui.theme.ProfilePictureBlue
import com.dluvian.nozzle.ui.theme.ProfilePictureBrown
import com.dluvian.nozzle.ui.theme.ProfilePictureCyan
import com.dluvian.nozzle.ui.theme.ProfilePictureGreen
import com.dluvian.nozzle.ui.theme.ProfilePictureLavender
import com.dluvian.nozzle.ui.theme.ProfilePictureLime
import com.dluvian.nozzle.ui.theme.ProfilePictureMagenta
import com.dluvian.nozzle.ui.theme.ProfilePictureMaroon
import com.dluvian.nozzle.ui.theme.ProfilePictureMint
import com.dluvian.nozzle.ui.theme.ProfilePictureNavy
import com.dluvian.nozzle.ui.theme.ProfilePictureOlive
import com.dluvian.nozzle.ui.theme.ProfilePictureOrange
import com.dluvian.nozzle.ui.theme.ProfilePicturePink
import com.dluvian.nozzle.ui.theme.ProfilePicturePurple
import com.dluvian.nozzle.ui.theme.ProfilePictureRed
import com.dluvian.nozzle.ui.theme.ProfilePictureTeal
import com.dluvian.nozzle.ui.theme.ProfilePictureYellow

private val bottomLeft = Offset(0f, Float.POSITIVE_INFINITY)
private val topRight = Offset(Float.POSITIVE_INFINITY, 0f)
private const val MAX_HEX_LEN = 7

@Composable
fun getDefaultPictureBrush(pubkey: String): Brush {
    if (pubkey.isBlank()) return SolidColor(Color.Transparent)

    val firstNumber = pubkey
        .dropWhile { it == '0' }
        .take(MAX_HEX_LEN)
        .ifEmpty { "0" }
        .toInt(radix = 16)
    val secondNumber = pubkey
        .dropLastWhile { it == '0' }
        .takeLast(MAX_HEX_LEN)
        .ifEmpty { "0" }
        .toInt(radix = 16)

    val gradient = listOf(getColor(firstNumber), getColor(secondNumber))

    return when (firstNumber % 4) {
        0 -> Brush.linearGradient(gradient)
        1 -> Brush.linearGradient(
            colors = gradient,
            start = bottomLeft,
            end = topRight,
        )

        2 -> Brush.horizontalGradient(gradient)
        else -> Brush.verticalGradient(gradient)
    }
}

private fun getColor(number: Int): Color {
    return when (number % 20) {
        0 -> ProfilePictureRed
        1 -> ProfilePictureGreen
        2 -> ProfilePictureYellow
        3 -> ProfilePictureBlue
        4 -> ProfilePictureOrange
        5 -> ProfilePicturePurple
        6 -> ProfilePictureCyan
        7 -> ProfilePictureMagenta
        8 -> ProfilePictureLime
        9 -> ProfilePicturePink
        10 -> ProfilePictureTeal
        11 -> ProfilePictureLavender
        12 -> ProfilePictureBrown
        13 -> ProfilePictureMint
        14 -> ProfilePictureOlive
        15 -> ProfilePictureApricot
        16 -> ProfilePictureMaroon
        17 -> ProfilePictureNavy
        else -> ProfilePictureBeige
    }
}
