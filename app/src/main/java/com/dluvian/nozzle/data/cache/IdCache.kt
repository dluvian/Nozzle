package com.dluvian.nozzle.data.cache

import java.util.Collections

class IdCache : IIdCache {
    private val postIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val pubkeys = Collections.synchronizedSet(mutableSetOf<String>())

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
}
