package com.dluvian.nozzle.model

sealed class AuthorSelection {
    abstract fun isContactsOnly(): Boolean
}

object Everyone : AuthorSelection() {
    override fun isContactsOnly() = false
}

object Contacts : AuthorSelection() {
    override fun isContactsOnly() = true
}

class SingleAuthor(val pubkey: String) : AuthorSelection() {
    override fun isContactsOnly() = false
}
