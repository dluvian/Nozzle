package com.dluvian.nozzle.data.utils


object UrlUtils {
    private val urlPattern = Regex(pattern = "https?://[^\\s]+")
    private val wssPattern =
        Regex("^(wss)://[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(:(\\d{1,5}))?(/.*)?\$")
    const val WEBSOCKET_PREFIX = "wss://"
    const val MAX_URL_LENGTH = 35

    val mediaSuffixes = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")

    fun extractUrls(extractFrom: String) = urlPattern.findAll(extractFrom).toList()

    fun shortenUrl(url: String): String {
        val noProtocol = url.removePrefix("https://").removePrefix("http://")

        return if (noProtocol.length <= MAX_URL_LENGTH) noProtocol
        else noProtocol.take(MAX_URL_LENGTH) + "…"
    }

    fun String.removeTrailingSlashes() =
        this.trim().dropLastWhile { lastChar -> lastChar == '/' || lastChar == ' ' }

    fun String.isWebsocketUrl() = wssPattern.matches(this)

    fun String.removeWebsocketPrefix() = this.removePrefix(WEBSOCKET_PREFIX)
}

