package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.PostWithMeta


interface IRelayProvider {
    fun getReadRelays(): List<String>
    fun getWriteRelays(): List<String>
    fun getPostRelays(posts: List<PostWithMeta>): List<String>
    suspend fun getAutopilotRelays(): Map<String, Set<String>>
    suspend fun getReadRelaysOfPubkey(pubkey: String): List<String>
    suspend fun getWriteRelaysOfPubkey(pubkey: String): List<String>
}
