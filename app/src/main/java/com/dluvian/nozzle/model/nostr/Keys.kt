package com.dluvian.nozzle.model.nostr

data class Keys(
    val pubkey: ByteArray,
    val privkey: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Keys

        if (!pubkey.contentEquals(other.pubkey)) return false
        if (!privkey.contentEquals(other.privkey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pubkey.contentHashCode()
        result = 31 * result + privkey.contentHashCode()
        return result
    }
}
