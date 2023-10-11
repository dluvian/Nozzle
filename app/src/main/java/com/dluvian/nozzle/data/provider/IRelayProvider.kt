package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay


interface IRelayProvider {
    fun getReadRelays(): List<String>
    fun getWriteRelays(): List<String>
    fun getNip65Relays(): List<Nip65Relay>
    suspend fun getReadRelaysOfPubkey(pubkey: String): List<String>
    suspend fun getWriteRelaysOfPubkey(pubkey: String): List<String>
    suspend fun getWriteRelaysOfPubkeys(pubkeys: Collection<String>): Map<Pubkey, List<Relay>>
    suspend fun getRelaysOfContacts(): List<Relay>
}
