package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay


interface IRelayProvider {
    fun getReadRelays(): List<String>
    fun getWriteRelays(): List<String>
    suspend fun getReadRelaysOfPubkey(pubkey: String): List<String>
    suspend fun getWriteRelaysOfPubkey(pubkey: String): List<String>
    suspend fun getWriteRelaysOfPubkeys(pubkeys: Collection<String>): Map<Pubkey, List<Relay>>
}
