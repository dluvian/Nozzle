package com.dluvian.nozzle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HyperlinkBlue = Color(0xFF007AFF)
val Orange500 = Color(0xFFFF9800)

// Colors from https://sashamaps.net/docs/resources/20-colors/
val ProfilePictureMaroon = Color(0xFF800000)
val ProfilePictureBrown = Color(0xFF9A6324)
val ProfilePictureOlive = Color(0xFF808000)
val ProfilePictureTeal = Color(0xFF469990)
val ProfilePictureNavy = Color(0xFF000075)
val ProfilePictureRed = Color(0xFFe6194B)
val ProfilePictureOrange = Color(0xFFf58231)
val ProfilePictureYellow = Color(0xFFffe119)
val ProfilePictureLime = Color(0xFFbfef45)
val ProfilePictureGreen = Color(0xFF3cb44b)
val ProfilePictureCyan = Color(0xFF42d4f4)
val ProfilePictureBlue = Color(0xFF4363d8)
val ProfilePicturePurple = Color(0xFF911eb4)
val ProfilePictureMagenta = Color(0xFFf032e6)
val ProfilePicturePink = Color(0xFFfabed4)
val ProfilePictureApricot = Color(0xFFffd8b1)
val ProfilePictureBeige = Color(0xFFffd8b1)
val ProfilePictureMint = Color(0xFFaaffc3)
val ProfilePictureLavender = Color(0xFFdcbeff)

val HintGray: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
