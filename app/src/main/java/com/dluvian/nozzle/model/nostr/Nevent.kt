package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.nostr.utils.TLVAuthor
import com.dluvian.nozzle.data.nostr.utils.TLVDefault
import com.dluvian.nozzle.data.nostr.utils.TLVEntry
import com.dluvian.nozzle.data.nostr.utils.TLVRelay
import com.dluvian.nozzle.data.utils.UrlUtils
import com.dluvian.nozzle.data.utils.toHexString

data class Nevent(
    val eventId: String,
    val relays: List<String>,
    val pubkey: String?,
) {
    companion object {
        fun fromTLVEntries(tlvEntries: List<TLVEntry>): Nevent? {
            if (tlvEntries.isEmpty()) return null
            val eventId =
                tlvEntries.find { it is TLVDefault }?.getBytes()?.toHexString() ?: return null
            val relays = tlvEntries.filterIsInstance<TLVRelay>()
                .map { it.getBytes().decodeToString() }
                .filter { UrlUtils.isWebsocketUrl(it) }
            val pubkey = tlvEntries.find { it is TLVAuthor }?.getBytes()?.toHexString()

            return Nevent(eventId = eventId, relays = relays, pubkey = pubkey)
        }
    }
}
