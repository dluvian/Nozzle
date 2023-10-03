package com.dluvian.nozzle.model.nostr.nip05

import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay

data class Nip05Response(
    val names: Map<String, Pubkey>?,
    val relays: Map<Pubkey, List<Relay>>?
)
