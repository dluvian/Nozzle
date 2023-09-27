package com.dluvian.nozzle.data.nostr.nip05

import kotlinx.coroutines.delay

class Nip05Resolver : INip05Resolver {
    override suspend fun resolve(nip05: String): Nip05Result? {
        delay(5000L)
        return null
    }

    override fun isNip05(nip05: String): Boolean {
        return true
    }
}