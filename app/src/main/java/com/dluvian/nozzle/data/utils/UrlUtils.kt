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
    private val whitespacePattern by lazy { Regex("\\s+") }
    private val mediaSuffixes = listOf(".jpg", ".jpeg", ".png", ".gif")

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

    fun getAppendedMediaUrl(content: String): String? {
        val lastWord = content.split(whitespacePattern).lastOrNull()
        return lastWord?.let {
            if (mediaSuffixes.any { suffix -> lastWord.endsWith(suffix) }) {
                val match = urlPattern.find(it)?.value
                if (it == match) it else null
            } else null
        }
    }

    fun removeWebsocketPrefix(url: String) = url.removePrefix("wss://")
}

