package com.dluvian.nozzle.data.utils

object HashtagUtils {
    private val hashtagPattern = Regex("#\\w+")

    fun extractHashtags(extractFrom: String) = hashtagPattern.findAll(extractFrom).toList()

    fun extractHashtagValues(extractFrom: String) = hashtagPattern.findAll(extractFrom)
        .map { it.value.removeHashtagPrefix() }
        .toList()

    fun isHashtag(toCheck: String) = hashtagPattern.matches(toCheck)

    fun String.removeHashtagPrefix() = this.removePrefix("#")
}
