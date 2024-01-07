package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.nostr.utils.KeyUtils.isValidHexKey
import com.dluvian.nozzle.data.utils.UrlUtils.isWebsocketUrl
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
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
            if (!isValidHexKey(pubkey)) return null
            val relays = tlvEntries.filterIsInstance<TLVRelay>()
                .map { it.value.decodeToString().removeTrailingSlashes() }
                .filter { it.isWebsocketUrl() }

            return Nprofile(pubkey = pubkey, relays = relays)
        }
    }
}
