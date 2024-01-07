package com.dluvian.nozzle.data.nostr

import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.SubId
import com.dluvian.nozzle.model.nostr.Event

interface NostrListener {
    fun onOpen(relay: Relay, msg: String)
    fun onEvent(
        subscriptionId: SubId,
        event: Event,
        relayUrl: Relay?
    )

    fun onError(relay: Relay, msg: String, throwable: Throwable? = null)
    fun onEOSE(relay: Relay, subscriptionId: SubId)
    fun onClosed(relay: Relay, subscriptionId: SubId, reason: String)
    fun onClose(relay: Relay, reason: String)
    fun onFailure(relay: Relay, msg: String?, throwable: Throwable? = null)
    fun onOk(relay: Relay, id: String, accepted: Boolean, msg: String)
}
