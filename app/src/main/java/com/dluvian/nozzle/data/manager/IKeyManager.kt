package com.dluvian.nozzle.data.manager

import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.model.nostr.Keys

interface IKeyManager : IPubkeyProvider {
    fun getPrivkey(): String
    fun getNsec(): String
    fun setPrivkey(privkey: String)
    fun getKeys(): Keys
}
