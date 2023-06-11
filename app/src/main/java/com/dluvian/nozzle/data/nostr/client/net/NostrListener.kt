package com.dluvian.nozzle.data.nostr.client.net

interface NostrListener {
    fun onOpen(msg: String)
    fun onEvent(
        subscriptionId: String,
        event: com.dluvian.nozzle.data.nostr.client.model.Event,
        relayUrl: String?
    )

    fun onError(msg: String, throwable: Throwable? = null)
    fun onEOSE(subscriptionId: String)
    fun onClose(reason: String)
    fun onFailure(msg: String?, throwable: Throwable? = null)
    fun onOk(id: String)
}
