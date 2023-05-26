package com.dluvian.nozzle.data.manager

import com.dluvian.nostrclientkt.model.Keys
import com.dluvian.nozzle.data.provider.IPubkeyProvider

interface IKeyManager : IPubkeyProvider {
    fun getPrivkey(): String
    fun getNsec(): String
    fun setPrivkey(privkey: String)
    fun getKeys(): Keys
}
