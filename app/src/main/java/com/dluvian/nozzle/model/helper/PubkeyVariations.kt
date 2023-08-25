package com.dluvian.nozzle.model.helper

import com.dluvian.nozzle.data.utils.getShortenedNpub
import com.dluvian.nozzle.data.utils.hexToNpub

data class PubkeyVariations(val pubkey: String, val npub: String, val shortenedNpub: String) {
    companion object {
        fun fromPubkey(pubkey: String): PubkeyVariations {
            val npub = hexToNpub(pubkey)
            return PubkeyVariations(
                pubkey = pubkey,
                npub = npub,
                shortenedNpub = getShortenedNpub(npub)
            )
        }
    }
}
