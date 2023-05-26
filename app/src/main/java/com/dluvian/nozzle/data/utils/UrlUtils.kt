package com.dluvian.nozzle.data.utils

import android.net.Uri


// TODO: Find a good regex. This is sloppy
private val urlPattern = Regex(
    pattern = "(?:^|[\\W])((http)(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
    options = setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
)

fun extractUrls(url: String?): List<String> {
    url ?: return listOf()

    val result = mutableListOf<String>()
    urlPattern.findAll(url).forEach { match ->
        result.add(match.value)
    }

    return result
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
