package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.nostr.utils.KeyUtils.isValidPubkey
import com.dluvian.nozzle.data.utils.UrlUtils
import com.dluvian.nozzle.data.utils.toHexString

data class Nprofile(
    val pubkey: String,
    val relays: List<String> = emptyList(),
) {
    companion object {
        fun fromTLVEntries(tlvEntries: List<TLVEntry>): Nprofile? {
            if (tlvEntries.isEmpty()) return null
            val pubkey = tlvEntries.find { it is TLVDefault }
                ?.value
                ?.toHexString()
                ?: return null
            if (!isValidPubkey(pubkey)) return null
            // TODO: Remove trailing slashes with util function
            val relays = tlvEntries.filterIsInstance<TLVRelay>()
                .map { it.value.decodeToString().removeSuffix("/") }
                .filter { UrlUtils.isWebsocketUrl(it) }

            return Nprofile(pubkey = pubkey, relays = relays)
        }
    }
}
