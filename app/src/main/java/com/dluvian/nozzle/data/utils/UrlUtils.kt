package com.dluvian.nozzle.data.utils

import android.net.Uri


object UrlUtils {
    private val urlPattern = Regex(
        pattern = "(?:^|[\\W])((http)(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
    )
    val mediaSuffixes = listOf(".jpg", ".jpeg", ".png", ".gif")

    fun extractUrls(url: String?): List<String> {
        return url?.let { urlPattern.findAll(it).map { match -> match.value }.toList() }
            ?: emptyList()
    }

    fun fixUrl(url: String): String {
        val trimmed = url.trim()
        return if (!trimmed.startsWith("http://")
            && !trimmed.startsWith("https://")
        ) {
            Uri.parse("http://$trimmed")
        } else {
            Uri.parse(trimmed)
        }.toString()
    }
}

