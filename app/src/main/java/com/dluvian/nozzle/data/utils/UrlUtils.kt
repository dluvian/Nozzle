package com.dluvian.nozzle.data.utils


object UrlUtils {
    private val urlPattern = Regex(pattern = "https?://[^\\s]+")
    private val wssPattern = Regex("^wss://[^\\s]+\$")

    val mediaSuffixes = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")

    fun extractUrls(extractFrom: String) = urlPattern.findAll(extractFrom).toList()

    fun String.removeTrailingSlashes() = this.trim().dropLastWhile { lastChar -> lastChar == '/' }

    fun String.isWebsocketUrl() = wssPattern.matches(this)

    fun String.removeWebsocketPrefix() = this.removePrefix("wss://")
}

