package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.utils.decodeHex
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile
import fr.acinq.secp256k1.Hex

private const val NPUB = "npub"
private const val NSEC = "nsec"
private const val NOTE = "note"
private const val NEVENT = "nevent"
private const val NPROFILE = "nprofile"

private const val URI = "nostr:"

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
    if (!npub.startsWith(NPUB + 1)) return Result.failure(IllegalArgumentException())
    return runCatching { Hex.encode(Bech32.decodeBytes(npub).second) }
}

fun note1ToHex(noteId: String): Result<String> {
    if (!noteId.startsWith(NOTE + 1)) return Result.failure(IllegalArgumentException())
    return runCatching { Hex.encode(Bech32.decodeBytes(noteId).second) }
}

fun nsecToHex(nsec: String): Result<String> {
    if (!nsec.startsWith(NSEC + 1)) return Result.failure(IllegalArgumentException())
    return runCatching { Hex.encode(Bech32.decodeBytes(nsec).second) }
}

fun readNevent(nevent: String): Nevent? {
    if (!nevent.startsWith(NEVENT + 1)) return null
    val tlvResult = TLVUtils.readTLVEntries(Bech32.decodeBytes(nevent).second)
    if (tlvResult.isFailure) return null

    return Nevent.fromTLVEntries(tlvResult.getOrNull().orEmpty())
}

fun readNprofile(nprofile: String): Nprofile? {
    if (!nprofile.startsWith(NPROFILE + 1)) return null
    val tlvResult = TLVUtils.readTLVEntries(Bech32.decodeBytes(nprofile).second)
    if (tlvResult.isFailure) return null

    return Nprofile.fromTLVEntries(tlvResult.getOrNull().orEmpty())
}

fun readNeventUri(neventUri: String): Nevent? {
    return if (neventUri.startsWith(URI)) readNevent(neventUri.drop(URI.length))
    else null
}

fun readNprofileUri(nprofileUri: String): Nprofile? {
    return if (nprofileUri.startsWith(URI)) readNprofile(nprofileUri.drop(URI.length))
    else null
}

fun note1UriToHex(note1Uri: String): String? {
    return if (note1Uri.startsWith(URI)) note1ToHex(note1Uri.drop(URI.length)).getOrNull()
    else null
}
