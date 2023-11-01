package com.dluvian.nozzle.model.nostr

data class RelayedEvent(
    val event: Event,
    val relayUrl: String
)
