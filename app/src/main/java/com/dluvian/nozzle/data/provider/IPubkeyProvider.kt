package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.Pubkey

interface IPubkeyProvider {
    fun getActivePubkey(): Pubkey
    fun getActiveNpub(): String
    fun isOneself(pubkey: Pubkey) = pubkey == getActivePubkey()
}
