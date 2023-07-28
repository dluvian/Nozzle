package com.dluvian.nozzle.data.utils

object NostrUtils {
    private val usernamePattern by lazy { Regex("\\w[\\w\\-]+\\w") }
    private val whitespacePattern by lazy { Regex("\\s+") }

    fun isValidUsername(username: String): Boolean {
        return usernamePattern.matches(username)
    }

    fun getAppendedNostrNote1(content: String): String? {
        val lastWord = content.split(whitespacePattern).lastOrNull()
        return lastWord?.let { if (it.startsWith("nostr:note1")) it else null }
    }
}
