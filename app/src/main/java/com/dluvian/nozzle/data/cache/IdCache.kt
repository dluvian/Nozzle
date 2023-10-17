package com.dluvian.nozzle.data.cache

import java.util.Collections

class IdCache : IIdCache {
    private val postIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val pubkeys = Collections.synchronizedSet(mutableSetOf<String>())
    private val nip65Pubkeys = Collections.synchronizedSet(mutableSetOf<String>())
    private val contactListAuthors = Collections.synchronizedSet(mutableSetOf<String>())

    override fun addPostId(id: String): Boolean {
        return postIds.add(id)
    }

    override fun getPostIds(): Set<String> {
        return postIds.toSet()
    }

    override fun addPubkey(pubkey: String): Boolean {
        return pubkeys.add(pubkey)
    }

    override fun getPubkeys(): Set<String> {
        return pubkeys.toSet()
    }

    override fun addNip65Author(pubkey: String): Boolean {
        return nip65Pubkeys.add(pubkey)
    }

    override fun getNip65Authors(): Set<String> {
        return nip65Pubkeys.toSet()
    }

    override fun addContactListAuthor(pubkey: String): Boolean {
        return contactListAuthors.add(pubkey)
    }

    override fun getContactListAuthors(): Set<String> {
        return contactListAuthors.toSet()
    }

    override fun removePostId(postId: String) {
        postIds.remove(postId)
    }
}
