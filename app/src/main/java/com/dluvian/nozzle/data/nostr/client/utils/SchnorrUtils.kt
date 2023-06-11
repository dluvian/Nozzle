package com.dluvian.nozzle.data.nostr.client.utils

import fr.acinq.secp256k1.Secp256k1


object SchnorrUtils {
    val secp256k1 = Secp256k1.get()

    fun sign(data: ByteArray, privKey: ByteArray): ByteArray {
        return secp256k1.signSchnorr(data, privKey, null)
    }
}
