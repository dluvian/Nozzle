package com.dluvian.nozzle.ui.components.text

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.data.utils.extractUrls
import com.dluvian.nozzle.data.utils.fixUrl
import com.dluvian.nozzle.ui.theme.HyperlinkBlue
import com.dluvian.nozzle.ui.theme.Typography

private const val URL_TAG = "URL"

@Composable
fun HyperlinkedText(
    text: String,
    onClickNonLink: () -> Unit,
    maxLines: Int? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    if (text.isNotBlank()) {
        val textColor = colors.onSurface
        val annotatedString =
            remember(text) { buildAnnotatedString(text = text, textColor = textColor) }
        val uriHandler = LocalUriHandler.current
        ClickableText(
            text = annotatedString,
            maxLines = maxLines ?: 1024,
            overflow = overflow,
            onClick = {
                val url = annotatedString
                    .getStringAnnotations(URL_TAG, it, it)
                    .firstOrNull()
                if (url != null) {
                    uriHandler.openUri(fixUrl(url.item))
                } else {
                    onClickNonLink()
                }
            }
        )
    }
}

private fun buildAnnotatedString(text: String, textColor: Color): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        extractUrls(text).forEach { url ->
            val startIndex = text.indexOf(url)
            val endIndex = startIndex + url.length
            addStyle(
                style = SpanStyle(
                    color = HyperlinkBlue,
                    textDecoration = TextDecoration.Underline
                ),
                start = startIndex,
                end = endIndex
            )
            addStringAnnotation(
                tag = URL_TAG,
                annotation = url,
                start = startIndex,
                end = endIndex
            )
        }
        addStyle(
            style = Typography.body1.toSpanStyle().copy(color = textColor),
            start = 0,
            end = text.length
        )
    }
}
