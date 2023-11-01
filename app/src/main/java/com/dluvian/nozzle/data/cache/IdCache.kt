package com.dluvian.nozzle.data.cache

import java.util.Collections

class IdCache : IIdCache {
    private val postIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val pubkeys = Collections.synchronizedSet(mutableSetOf<String>())
    private val nip65Pubkeys = Collections.synchronizedSet(mutableSetOf<String>())
    private val contactListAuthors = Collections.synchronizedSet(mutableSetOf<String>())

    override fun addPostIds(ids: Collection<String>): Boolean = postIds.addAll(ids)
    override fun containsPostId(id: String): Boolean = postIds.contains(id)
    override fun getPostIds(): Set<String> = postIds.toSet()
    override fun clearPostIds() = postIds.clear()

    override fun addPubkeys(pubkeys: Collection<String>) = this.pubkeys.addAll(pubkeys)
    override fun getPubkeys(): Set<String> = pubkeys.toSet()
    override fun clearPubkeys() = pubkeys.clear()

    override fun addNip65Author(pubkey: String): Boolean = nip65Pubkeys.add(pubkey)
    override fun getNip65Authors(): Set<String> = nip65Pubkeys.toSet()
    override fun clearNip65Authors() = nip65Pubkeys.clear()

    override fun addContactListAuthor(pubkey: String): Boolean = contactListAuthors.add(pubkey)
    override fun getContactListAuthors(): Set<String> = contactListAuthors.toSet()
    override fun clearContactListAuthors() = contactListAuthors.clear()
}
