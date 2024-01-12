package com.dluvian.nozzle.model.feedFilter

import com.dluvian.nozzle.model.Pubkey

sealed class AuthorFilter
data object Global : AuthorFilter()
data object Friends : AuthorFilter()
data object FriendCircle : AuthorFilter()
data class SingularPerson(val pubkey: Pubkey) : AuthorFilter()
