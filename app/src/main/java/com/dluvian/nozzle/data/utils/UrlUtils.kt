package com.dluvian.nozzle.data.utils


object UrlUtils {
    private val urlPattern by lazy {
        Regex(pattern = "https?://[^\\s]+")
    }

    val mediaSuffixes = listOf(".jpg", ".jpeg", ".png", ".gif")


    fun extractUrls(extractFrom: String): List<MatchResult> {
        return urlPattern.findAll(extractFrom).toList()
    }

    fun cleanUrl(url: String) = url.trim().dropLastWhile { lastChar -> lastChar == '/' }

    // TODO: Improve this
    fun isWebsocketUrl(url: String) = url.startsWith("wss://")
            && url.length >= 9
            && url.contains(".")

    fun removeWebsocketPrefix(url: String) = url.removePrefix("wss://")
}

