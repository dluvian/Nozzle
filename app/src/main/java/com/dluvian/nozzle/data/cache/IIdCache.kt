package com.dluvian.nozzle.data.cache

interface IIdCache {
    fun addPostIds(ids: Collection<String>): Boolean
    fun containsPostId(id: String): Boolean
    fun getPostIds(): Set<String>
    fun clearPostIds()

    fun addPubkeys(pubkeys: Collection<String>): Boolean
    fun getPubkeys(): Set<String>
    fun clearPubkeys()

    fun addNip65Author(pubkey: String): Boolean
    fun getNip65Authors(): Set<String>
    fun clearNip65Authors()

    fun addContactListAuthors(pubkeys: Collection<String>): Boolean
    fun getContactListAuthors(): Set<String>
    fun clearContactListAuthors()
}
