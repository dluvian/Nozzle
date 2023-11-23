package com.dluvian.nozzle.data.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.dluvian.nozzle.ui.theme.ProfilePictureApricot
import com.dluvian.nozzle.ui.theme.ProfilePictureBlue
import com.dluvian.nozzle.ui.theme.ProfilePictureBrown
import com.dluvian.nozzle.ui.theme.ProfilePictureCyan
import com.dluvian.nozzle.ui.theme.ProfilePictureGreen
import com.dluvian.nozzle.ui.theme.ProfilePictureLavender
import com.dluvian.nozzle.ui.theme.ProfilePictureLime
import com.dluvian.nozzle.ui.theme.ProfilePictureMagenta
import com.dluvian.nozzle.ui.theme.ProfilePictureMint
import com.dluvian.nozzle.ui.theme.ProfilePictureOlive
import com.dluvian.nozzle.ui.theme.ProfilePictureOrange
import com.dluvian.nozzle.ui.theme.ProfilePicturePink
import com.dluvian.nozzle.ui.theme.ProfilePicturePurple
import com.dluvian.nozzle.ui.theme.ProfilePictureRed
import com.dluvian.nozzle.ui.theme.ProfilePictureTeal
import com.dluvian.nozzle.ui.theme.ProfilePictureYellow

@Composable
fun getDefaultPictureBrush(pubkey: String): Brush {
    if (pubkey.isBlank()) return SolidColor(Color.Transparent)
    val maxHexLength = 7

    val firstNumber = pubkey
        .dropWhile { it == '0' }
        .take(maxHexLength)
        .ifEmpty { "0" }
        .toInt(radix = 16)
    val firstColor = getColor(firstNumber)

    val secondNumber = pubkey
        .dropLastWhile { it == '0' }
        .takeLast(maxHexLength)
        .ifEmpty { "0" }
        .toInt(radix = 16)
    val secondColor = getColor(secondNumber)

    val gradient = listOf(
        if (firstColor == secondColor) Color.Transparent else firstColor,
        secondColor
    )

    return when (firstNumber % 3) {
        0 -> Brush.linearGradient(gradient)
        1 -> Brush.horizontalGradient(gradient)
        else -> Brush.verticalGradient(gradient)
    }
}

private fun getColor(number: Int): Color {
    return when (number % 16) {
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
        else -> ProfilePictureApricot
    }
}
