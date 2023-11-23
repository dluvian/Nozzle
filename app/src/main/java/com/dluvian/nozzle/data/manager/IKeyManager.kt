package com.dluvian.nozzle.data.manager

import androidx.compose.runtime.State
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Keys

interface IKeyManager : IPubkeyProvider {
    val hasPrivkey: State<Boolean>
    fun getActiveNsec(): String
    suspend fun activatePubkey(pubkey: Pubkey)
    suspend fun addPrivkey(privkey: String)
    suspend fun deletePubkey(pubkey: Pubkey)
    fun getActiveKeys(): Keys
}
