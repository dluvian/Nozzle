package com.dluvian.nozzle.data.utils

import fr.acinq.secp256k1.Hex
import fr.acinq.secp256k1.Secp256k1
import java.security.SecureRandom

private val rnd = SecureRandom()
private val secp256k1 = Secp256k1.get()
private const val NPUB = "npub"
private const val NSEC = "nsec"
private const val NOTE = "note"

fun generatePrivkey(): String {
    val bytes = ByteArray(32)
    rnd.nextBytes(bytes)
    return bytes.toHex()
}

fun derivePubkey(privkey: String): String {
    val bytes = privkey.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
    return secp256k1.pubKeyCompress(secp256k1.pubkeyCreate(bytes))
        .copyOfRange(1, 33)
        .toHex()
}

fun hexToNpub(pubkey: String): String {
    return Bech32.encode(NPUB, pubkey.decodeHex())
}

fun hexToNsec(privkey: String): String {
    return Bech32.encode(NSEC, privkey.decodeHex())
}

fun hexToNote(postId: String): String {
    return Bech32.encode(NOTE, postId.decodeHex())
}

fun npubToHex(npub: String): Result<String> {
    if (!npub.startsWith("npub1")) return Result.failure(IllegalArgumentException())
    return try {
        Result.success(Hex.encode(Bech32.decodeBytes(npub).second))
    } catch (t: Throwable) {
        Result.failure(t)
    }
}

fun noteIdToHex(noteId: String): Result<String> {
    if (!noteId.startsWith("note1")) return Result.failure(IllegalArgumentException())
    return try {
        Result.success(Hex.encode(Bech32.decodeBytes(noteId).second))
    } catch (t: Throwable) {
        Result.failure(t)
    }
}

fun nsecToHex(nsec: String): Result<String> {
    if (!nsec.startsWith("nsec1")) return Result.failure(IllegalArgumentException())

    return try {
        Result.success(Hex.encode(Bech32.decodeBytes(nsec).second))
    } catch (t: Throwable) {
        Result.failure(t)
    }
}

fun isValidPrivkey(privkey: String): Boolean {
    return nsecToHex(privkey).isSuccess || (privkey.length == 64 && privkey.isHex())
}

private fun ByteArray.toHex(): String {
    return this.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}
