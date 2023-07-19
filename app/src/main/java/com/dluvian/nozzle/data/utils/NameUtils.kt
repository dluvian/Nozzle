package com.dluvian.nozzle.data.utils

fun getShortenedNpubFromPubkey(pubkey: String): String {
    return if (pubkey.length < 32) {
        ""
    } else {
        getShortenedNpub(hexToNpub(pubkey))
    }
}

fun getShortenedNpub(npub: String): String {
    return if (npub.length < 32) {
        ""
    } else {
        "${npub.take(8)}::${npub.takeLast(4)}"
    }
}