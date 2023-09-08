package com.dluvian.nozzle.data.utils

import android.net.Uri


object UrlUtils {
    private val urlPattern by lazy {
        Regex(
            pattern = "(?:^|[\\W])((http)(s?)://|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
            options = setOf(
                RegexOption.IGNORE_CASE,
                RegexOption.DOT_MATCHES_ALL
            )
        )
    }

    fun extractUrls(url: String?): List<String> {
        return url?.let { urlPattern.findAll(it).map { match -> match.value }.toList() }
            ?: emptyList()
    }

    fun cleanUrl(url: String) = url.trim().dropLastWhile { lastChar -> lastChar == '/' }

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

    // TODO: Improve this
    fun isWebsocketUrl(url: String) = url.startsWith("wss://")
            && url.length >= 9
            && url.contains(".")

    fun removeWebsocketPrefix(url: String) = url.removePrefix("wss://")
}

