package com.dluvian.nozzle.data.cache

interface IIdCache {
    fun addPostId(id: String): Boolean
    fun getPostIds(): Set<String>
    fun addPubkey(pubkey: String): Boolean
    fun getPubkeys(): Set<String>
}
