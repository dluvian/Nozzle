package com.dluvian.nozzle.data.utils

object HashtagUtils {
    private val hashtagPattern = Regex("#\\w+")

    fun extractHashtags(extractFrom: String) = hashtagPattern.findAll(extractFrom).toList()

    fun isHashtag(toCheck: String) = hashtagPattern.matches(toCheck)
}