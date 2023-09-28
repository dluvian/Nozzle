package com.dluvian.nozzle.data.nostr.nip05

import com.dluvian.nozzle.model.nostr.nip05.Nip05Result

interface INip05Resolver {

    suspend fun resolve(nip05: String): Nip05Result?

    fun isNip05(nip05: String): Boolean
}