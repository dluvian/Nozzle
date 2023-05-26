package com.dluvian.nostrclientkt.net

import com.dluvian.nostrclientkt.model.Event

interface NostrListener {
    fun onOpen(msg: String)
    fun onEvent(subscriptionId: String, event: Event, relayUrl: String?)
    fun onError(msg: String, throwable: Throwable? = null)
    fun onEOSE(subscriptionId: String)
    fun onClose(reason: String)
    fun onFailure(msg: String?, throwable: Throwable? = null)
    fun onOk(id: String)
}
