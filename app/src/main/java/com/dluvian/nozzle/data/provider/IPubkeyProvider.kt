package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.model.Pubkey

interface IPubkeyProvider {
    fun getActivePubkey(): Pubkey
    fun getActiveNpub() = EncodingUtils.hexToNpub(getActivePubkey())
    fun isOneself(pubkey: Pubkey) = pubkey == getActivePubkey()
}
