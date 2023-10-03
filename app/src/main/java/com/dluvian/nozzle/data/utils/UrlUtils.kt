package com.dluvian.nozzle.data.utils


object UrlUtils {
    private val urlPattern by lazy {
        Regex(pattern = "https?://[^\\s]+")
    }

    val mediaSuffixes = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")


    fun extractUrls(extractFrom: String) = urlPattern.findAll(extractFrom).toList()

    fun String.removeTrailingSlashes() = this.trim().dropLastWhile { lastChar -> lastChar == '/' }

    fun String.isWebsocketUrl() = this.startsWith("wss://")
            && this.length >= 9
            && this.contains(".")

    fun String.removeWebsocketPrefix() = this.removePrefix("wss://")
}

