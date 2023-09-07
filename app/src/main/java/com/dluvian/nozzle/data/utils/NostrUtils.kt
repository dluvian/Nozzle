package com.dluvian.nozzle.data.utils

import com.dluvian.nozzle.data.nostr.utils.note1ToHex

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

    fun getHexFromNostrNote1(nostrNote1: String): String? {
        return note1ToHex(nostrNote1.removePrefix("nostr:"))
            .getOrNull()
    }
}
