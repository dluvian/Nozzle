package com.dluvian.nozzle.data.eventProcessor

import com.dluvian.nozzle.model.nostr.Event

interface IEventProcessor {
    fun process(event: Event, relayUrl: String?)
}
