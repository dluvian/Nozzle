package com.dluvian.nozzle.data.utils


object UrlUtils {
    private val urlPattern = Regex(pattern = "https?://[^\\s]+")
    private val wssPattern =
        Regex("^(wss)://[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(:(\\d{1,5}))?(/.*)?\$")
    const val WEBSOCKET_PREFIX = "wss://"
    private const val MAX_URL_LENGTH = 30

    // Video suffixes from https://developer.android.com/guide/topics/media/platform/supported-formats#video-formats
    private val mediaSuffixes = listOf(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg",
        ".mp4", ".webm", ".mkv", ".3gp"
    )

    fun String.hasMediaSuffix(): Boolean {
        if (mediaSuffixes.any { suffix -> this.endsWith(suffix) }) return true
        val url = this.split("#").firstOrNull() ?: return false
        return mediaSuffixes.any { suffix -> url.endsWith(suffix) }
    }

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

