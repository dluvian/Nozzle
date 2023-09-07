package com.dluvian.nozzle.data.utils

import fr.acinq.secp256k1.Hex

private const val HEX_CHARS = "0123456789abcdef"

fun String.isHex(): Boolean {
    return this.all { char -> HEX_CHARS.contains(char = char, ignoreCase = true) }
}

fun ByteArray.toHexString(): String {
    return Hex.encode(this)
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
