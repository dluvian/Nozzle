package com.dluvian.nozzle.data.utils

object NostrUtils {
    private val usernamePattern by lazy { Regex("\\w[\\w\\-]+\\w") }

    fun isValidUsername(username: String): Boolean {
        return usernamePattern.matches(username)
    }
}
