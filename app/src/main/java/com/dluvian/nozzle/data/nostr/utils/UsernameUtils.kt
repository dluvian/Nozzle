package com.dluvian.nozzle.data.nostr.utils

object UsernameUtils {
    private val usernamePattern by lazy { Regex("\\w[\\w\\-]+\\w") }

    fun isValidUsername(username: String): Boolean {
        return usernamePattern.matches(username)
    }
}
