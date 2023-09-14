package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.nostr.utils.KeyUtils.isValidPubkey
import com.dluvian.nozzle.data.utils.UrlUtils
import com.dluvian.nozzle.data.utils.toHexString

data class Nevent(
    val eventId: String,
    val relays: List<String> = emptyList(),
    val pubkey: String? = null,
) {
    companion object {
        fun fromTLVEntries(tlvEntries: List<TLVEntry>): Nevent? {
            if (tlvEntries.isEmpty()) return null
            val eventId = tlvEntries.find { it is TLVDefault }
                ?.value
                ?.toHexString()
                ?: return null
            if (!isValidPubkey(eventId)) return null
            // TODO: Remove trailing slashes with util function
            val relays = tlvEntries.filterIsInstance<TLVRelay>()
                .map { it.value.decodeToString().removeSuffix("/") }
                .filter { UrlUtils.isWebsocketUrl(it) }
            val pubkey = tlvEntries.find { it is TLVAuthor }?.value?.toHexString()?.let {
                if (!isValidPubkey(it)) null else it
            }

            return Nevent(eventId = eventId, relays = relays, pubkey = pubkey)
        }
    }
}
