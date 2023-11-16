package com.dluvian.nozzle.model

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.hexToNpub
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpub


data class PubkeyVariations(val pubkey: String, val npub: String, val shortenedNpub: String) {
    companion object {
        fun fromPubkey(pubkey: String): PubkeyVariations {
            val npub = hexToNpub(pubkey)
            return PubkeyVariations(
                pubkey = pubkey,
                npub = npub,
                shortenedNpub = getShortenedNpub(npub) ?: npub
            )
        }
    }
}
