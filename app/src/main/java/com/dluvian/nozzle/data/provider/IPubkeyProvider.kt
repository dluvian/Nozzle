package com.dluvian.nozzle.data.provider

interface IPubkeyProvider {
    fun getPubkey(): String
    fun getNpub(): String
    fun isOneself(pubkey: String) = pubkey == getPubkey()
}
