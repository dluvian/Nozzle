package com.dluvian.nozzle.data.annotatedContent

import androidx.compose.ui.text.AnnotatedString

interface IAnnotatedContentBuilder {
    fun annotateContent(content: String): AnnotatedString
}