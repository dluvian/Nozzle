package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.utils.decodeHex
import com.dluvian.nozzle.model.nostr.Nevent
import fr.acinq.secp256k1.Hex

private const val NPUB = "npub"
private const val NSEC = "nsec"
private const val NOTE = "note"

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

fun hexToNpub(pubkey: String): String {
    return Bech32.encode(NPUB, pubkey.decodeHex())
}

fun hexToNsec(privkey: String): String {
    return Bech32.encode(NSEC, privkey.decodeHex())
}

fun hexToNote1(postId: String): String {
    return Bech32.encode(NOTE, postId.decodeHex())
}

fun npubToHex(npub: String): Result<String> {
    if (!npub.startsWith("npub1")) return Result.failure(IllegalArgumentException())
    return runCatching { Hex.encode(Bech32.decodeBytes(npub).second) }
}

fun note1ToHex(noteId: String): Result<String> {
    if (!noteId.startsWith("note1")) return Result.failure(IllegalArgumentException())
    return runCatching { Hex.encode(Bech32.decodeBytes(noteId).second) }
}

fun nsecToHex(nsec: String): Result<String> {
    if (!nsec.startsWith("nsec1")) return Result.failure(IllegalArgumentException())
    return runCatching { Hex.encode(Bech32.decodeBytes(nsec).second) }
}

fun readNevent(nevent: String): Nevent? {
    if (!nevent.startsWith("nevent1")) return null
    val tlvResult = TLVUtils.readTLVEntries(Bech32.decodeBytes(nevent).second)
    if (tlvResult.isFailure) return null

    return Nevent.fromTLVEntries(tlvResult.getOrNull().orEmpty())
}
