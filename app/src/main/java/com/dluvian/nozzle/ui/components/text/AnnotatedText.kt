package com.dluvian.nozzle.ui.components.text

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.ui.theme.Typography

@OptIn(ExperimentalTextApi::class)
@Composable
fun AnnotatedText(
    text: AnnotatedString,
    onClickNonLink: () -> Unit,
    onNavigateToId: (String) -> Unit,
    maxLines: Int? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    val uriHandler = LocalUriHandler.current
    val textColor = MaterialTheme.colors.onSurface
    val annotatedString = remember(text) { useDefaultTextStyle(text = text, textColor = textColor) }
    ClickableText(
        text = annotatedString,
        maxLines = maxLines ?: 1024,
        overflow = overflow,
        onClick = {
            val url = text.getUrlAnnotations(it, it).firstOrNull()
            if (url != null) uriHandler.openUri(url.item.url)
            else {
                val other = text.getStringAnnotations(it, it).firstOrNull()
                if (other != null) onNavigateToId(other.item)
                else onClickNonLink()
            }
        }
    )
}

private fun useDefaultTextStyle(text: AnnotatedString, textColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val index = pushStyle(Typography.body1.toSpanStyle().copy(color = textColor))
        append(text)
        pop(index)
    }
}
