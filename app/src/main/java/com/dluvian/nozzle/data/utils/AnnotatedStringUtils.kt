package com.dluvian.nozzle.data.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation

object AnnotatedStringUtils {
    fun AnnotatedString.Builder.pushAnnotatedString(
        tag: String,
        annotation: String,
        style: SpanStyle,
        text: String
    ) {
        pushStringAnnotation(tag = tag, annotation = annotation)
        pushStyledString(style = style, text = text)
        pop()
    }

    @OptIn(ExperimentalTextApi::class)
    fun AnnotatedString.Builder.pushStyledUrlAnnotation(
        url: String,
        style: SpanStyle
    ) {
        pushUrlAnnotation(UrlAnnotation(url = url))
        pushStyledString(style = style, text = UrlUtils.shortenUrl(url))
        pop()
    }

    private fun AnnotatedString.Builder.pushStyledString(style: SpanStyle, text: String) {
        pushStyle(style = style)
        append(text)
        pop()
    }
}
