package com.dluvian.nozzle.data.cache

interface IIdCache {
    fun addPostId(id: String): Boolean
    fun containsPostId(id: String): Boolean
    fun getPostIds(): Set<String>
    fun removePostId(postId: String)
    fun clearPostIds()

    fun addPubkey(pubkey: String): Boolean
    fun getPubkeys(): Set<String>
    fun clearPubkeys()

    fun addNip65Author(pubkey: String): Boolean
    fun getNip65Authors(): Set<String>
    fun clearNip65Authors()

    fun addContactListAuthor(pubkey: String): Boolean
    fun getContactListAuthors(): Set<String>
    fun clearContactListAuthors()
}
