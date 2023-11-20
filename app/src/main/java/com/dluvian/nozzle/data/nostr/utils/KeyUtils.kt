package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.npubToHex
import com.dluvian.nozzle.data.utils.isHex
import fr.acinq.secp256k1.Secp256k1
import java.security.SecureRandom


object KeyUtils {
    private val rnd = SecureRandom()
    private val secp256k1 = Secp256k1.get()

    private fun ByteArray.toHex(): String {
        return this.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
    }

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

    fun isValidPrivkeyHex(hex: String): Boolean {
        return isValidHexKey(hex) && hex.any { it != '0' } && hex.any { it != '1' }
    }

    fun isValidPubkey(pubkey: String): Boolean {
        return isValidHexKey(pubkey) || npubToHex(pubkey) != null
    }

    fun isValidHexKey(hexKey: String): Boolean {
        return hexKey.length == 64 && hexKey.isHex()
    }

}
