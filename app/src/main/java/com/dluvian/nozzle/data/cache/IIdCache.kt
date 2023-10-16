package com.dluvian.nozzle.data.cache

interface IIdCache {
    fun addPostId(id: String): Boolean
    fun getPostIds(): Set<String>

    fun addPubkey(pubkey: String): Boolean
    fun getPubkeys(): Set<String>

    fun addNip65Author(pubkey: String): Boolean
    fun getNip65Authors(): Set<String>

    fun addContactListAuthor(pubkey: String): Boolean
    fun getContactListAuthors(): Set<String>
}
