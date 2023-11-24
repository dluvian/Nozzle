package com.dluvian.nozzle.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.dluvian.nozzle.ui.theme.Shapes

@Composable
fun NumberedCategory(number: Int, category: String, onClick: () -> Unit) {
    val text = remember(number, category) {
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(number.toString())
                append(" ")
            }
            append(category)
        }
    }
    Text(
        modifier = Modifier
            .clip(Shapes.small)
            .clickable(onClick = onClick),
        text = text
    )
}
