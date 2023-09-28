package com.dluvian.nozzle.data.nostr.nip05

import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay

data class Nip05Response(
    val names: Map<String, Pubkey> = emptyMap(),
    val relays: Map<Pubkey, List<Relay>> = emptyMap()
)
