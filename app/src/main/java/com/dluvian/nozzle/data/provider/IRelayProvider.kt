package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay


interface IRelayProvider {
    fun getReadRelays(limit: Boolean): List<Relay>
    fun getWriteRelays(): List<Relay>
    fun getNip65Relays(): List<Nip65Relay>
    suspend fun getReadRelaysOfPubkey(pubkey: String): List<Relay>
    suspend fun getReadRelaysOfPubkeys(pubkeys: Collection<String>): Map<Pubkey, List<Relay>>
    suspend fun getWriteRelaysOfPubkey(pubkey: String): List<Relay>
    suspend fun getWriteRelaysByPubkeys(pubkeys: Collection<Pubkey>): Map<Pubkey, List<Relay>>
    suspend fun getRelaysOfContacts(): List<Relay>
}
