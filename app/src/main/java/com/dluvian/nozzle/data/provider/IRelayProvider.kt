package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.helper.Pubkey
import com.dluvian.nozzle.model.helper.Relay


interface IRelayProvider {
    fun getReadRelays(): List<String>
    fun getWriteRelays(): List<String>
    fun getPostRelays(posts: List<PostWithMeta>): List<String>
    suspend fun getAutopilotRelays(): Map<String, Set<String>>
    suspend fun getReadRelaysOfPubkey(pubkey: String): List<String>
    suspend fun getWriteRelaysOfPubkey(pubkey: String): List<String>
    suspend fun getWriteRelaysOfPubkeys(pubkeys: Collection<String>): Map<Pubkey, List<Relay>>
}
