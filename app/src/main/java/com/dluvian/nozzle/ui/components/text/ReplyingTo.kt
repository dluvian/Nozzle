package com.dluvian.nozzle.ui.components.text

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.LightGray21

@Composable
fun ReplyingTo(name: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = LightGray21)) {
                append(stringResource(id = R.string.replying_to))
                append(" ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = LightGray21)) {
                append(name)
            }
        },
        maxLines = 1
    )
}
