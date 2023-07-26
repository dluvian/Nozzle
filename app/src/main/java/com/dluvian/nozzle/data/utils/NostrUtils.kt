package com.dluvian.nozzle.data.utils

object NostrUtils {
    private val usernameRegex by lazy { Regex("\\w[\\w\\-]+\\w") }
    private val nostrUriPattern = Regex(
        pattern = "\\bnostr[\\p{Alnum}]*\\b",
        option = RegexOption.IGNORE_CASE
    )

    fun isValidUsername(username: String): Boolean {
        return usernameRegex.matches(username)
    }

    fun extractNostrUris(uri: String?): List<String> {
        return uri?.let { nostrUriPattern.findAll(it).map { match -> match.value }.toList() }
            ?: emptyList()
    }
}
