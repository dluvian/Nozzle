package com.dluvian.nozzle.data.nostr.client.utils

object NostrUtils {
    private val usernameRegex by lazy { Regex("\\w[\\w\\-]+\\w") }

    fun isValidUsername(username: String): Boolean {
        return usernameRegex.matches(username)
    }
}
