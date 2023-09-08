package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.nostr.utils.TLVDefault
import com.dluvian.nozzle.data.nostr.utils.TLVEntry
import com.dluvian.nozzle.data.nostr.utils.TLVRelay
import com.dluvian.nozzle.data.nostr.utils.isValidPubkey
import com.dluvian.nozzle.data.utils.UrlUtils
import com.dluvian.nozzle.data.utils.toHexString

data class Nprofile(
    val pubkey: String,
    val relays: List<String>,
) {
    companion object {
        fun fromTLVEntries(tlvEntries: List<TLVEntry>): Nprofile? {
            if (tlvEntries.isEmpty()) return null
            val pubkey = tlvEntries.find { it is TLVDefault }
                ?.getBytes()
                ?.toHexString()
                ?: return null
            if (!isValidPubkey(pubkey)) return null
            val relays = tlvEntries.filterIsInstance<TLVRelay>()
                .map { it.getBytes().decodeToString() }
                .filter { UrlUtils.isWebsocketUrl(it) }

            return Nprofile(pubkey = pubkey, relays = relays)
        }
    }
}
