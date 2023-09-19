package com.dluvian.nozzle.model

sealed class AuthorSelection(val isContactsOnly: Boolean)

data object Everyone : AuthorSelection(isContactsOnly = false)

data object Contacts : AuthorSelection(isContactsOnly = true)

class SingleAuthor(val pubkey: String) : AuthorSelection(isContactsOnly = false)
