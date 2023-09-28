package com.dluvian.nozzle.data.nostr.nip05

interface INip05Resolver {

    suspend fun resolve(nip05: String): Nip05Result?

    fun isNip05(nip05: String): Boolean
}