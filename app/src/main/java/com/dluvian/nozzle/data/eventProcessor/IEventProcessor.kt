package com.dluvian.nozzle.data.eventProcessor

import com.dluvian.nostrclientkt.model.Event

interface IEventProcessor {
    fun process(event: Event, relayUrl: String?)
}
