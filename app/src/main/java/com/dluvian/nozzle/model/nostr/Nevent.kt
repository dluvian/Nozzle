package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.nostr.utils.KeyUtils.isValidPubkey
import com.dluvian.nozzle.data.utils.UrlUtils.isWebsocketUrl
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import com.dluvian.nozzle.data.utils.toHexString

data class Nevent(
    val eventId: String,
    val relays: List<String> = emptyList(),
) {
    companion object {
        fun fromTLVEntries(tlvEntries: List<TLVEntry>): Nevent? {
            if (tlvEntries.isEmpty()) return null
            val eventId = tlvEntries.find { it is TLVDefault }
                ?.value
                ?.toHexString()
                ?: return null
            if (!isValidPubkey(eventId)) return null
            val relays = tlvEntries.filterIsInstance<TLVRelay>()
                .map { it.value.decodeToString().removeTrailingSlashes() }
                .filter { it.isWebsocketUrl() }

            return Nevent(eventId = eventId, relays = relays)
        }
    }
}
