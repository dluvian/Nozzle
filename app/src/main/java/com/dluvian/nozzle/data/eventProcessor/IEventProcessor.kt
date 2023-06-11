package com.dluvian.nozzle.data.eventProcessor

import com.dluvian.nozzle.data.nostr.client.model.Event

interface IEventProcessor {
    fun process(event: Event, relayUrl: String?)
}
