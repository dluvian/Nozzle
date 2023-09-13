package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.hexToNpub

object ShortenedNameUtils {
    fun getShortenedNpubFromPubkey(pubkey: String): String? {
        return if (pubkey.length < 32) {
            null
        } else {
            getShortenedNpub(hexToNpub(pubkey))
        }
    }

    fun getShortenedNpub(npub: String): String? {
        return if (!npub.startsWith(EncodingUtils.NPUB + 1) || npub.length < 32) {
            null
        } else {
            "${npub.take(8)}::${npub.takeLast(6)}"
        }
    }

    fun getShortenedNprofile(nprofile: String): String? {
        return if (!nprofile.startsWith(EncodingUtils.NPROFILE + 1) || nprofile.length < 32) {
            null
        } else {
            "${nprofile.take(8)}::${nprofile.takeLast(6)}"
        }
    }

    fun getShortenedNote1(note1: String): String? {
        return if (!note1.startsWith(EncodingUtils.NOTE + 1) || note1.length < 32) {
            null
        } else {
            "${note1.take(4)}::${note1.takeLast(6)}"
        }
    }

    fun getShortenedNevent(nevent: String): String? {
        return if (!nevent.startsWith(EncodingUtils.NEVENT + 1) || nevent.length < 32) {
            null
        } else {
            "${nevent.take(6)}::${nevent.takeLast(6)}"
        }
    }
}