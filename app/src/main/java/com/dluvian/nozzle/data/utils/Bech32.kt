package com.dluvian.nozzle.data.utils

/**
 * https://github.com/Giszmo/NostrPostr/blob/master/nostrpostrlib/src/main/java/nostr/postr/Bech32Util.kt
 */
object Bech32 {
    const val CHARSET: String = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"

    enum class Encoding(val constant: Int) {
        Bech32(1),
        Bech32WithoutChecksum(0),
    }

    // char -> 5 bits value
    private val map = Array<Byte>(255) { -1 }

    init {
        for (i in 0..CHARSET.lastIndex) {
            map[CHARSET[i].code] = i.toByte()
        }
    }

    private fun polymod(values: ByteArray): Int {
        var c = 1
        for (v_i in values) {
            val c0 = c.ushr(25) and 0xff
            c = c and 0x1ffffff shl 5 xor (v_i.toInt() and 0xff)
            if (c0 and 1 != 0) c = c xor 0x3b6a57b2
            if (c0 and 2 != 0) c = c xor 0x26508e6d
            if (c0 and 4 != 0) c = c xor 0x1ea119fa
            if (c0 and 8 != 0) c = c xor 0x3d4233dd
            if (c0 and 16 != 0) c = c xor 0x2a1462b3
        }
        return c
    }

    private fun expandHrp(hrp: String): ByteArray {
        val hrpLength = hrp.length
        val ret = ByteArray(hrpLength * 2 + 1)
        for (i in 0 until hrpLength) {
            val c = hrp[i].code and 0x7f
            ret[i] = (c.ushr(5) and 0x07).toByte()
            ret[i + hrpLength + 1] = (c and 0x1f).toByte()
        }
        ret[hrpLength] = 0
        return ret
    }

    private fun createChecksum(hrp: String, values: ByteArray): ByteArray {
        val hrpExpanded = expandHrp(hrp)
        val enc = ByteArray(hrpExpanded.size + values.size + 6)
        hrpExpanded.copyInto(enc)
        values.copyInto(enc, startIndex = 0, destinationOffset = hrpExpanded.size)

        val mod = polymod(enc) xor 1
        val ret = ByteArray(6)
        for (i in 0..5) {
            ret[i] = (mod.ushr(5 * (5 - i)) and 31).toByte()
        }
        return ret
    }

    fun encode(humanReadablePart: String, data: ByteArray): String {
        var hrp = humanReadablePart
        val as5Bit = convertTo5BitData(data)

        check(hrp.isNotEmpty())
        check(hrp.length <= 83)

        hrp = hrp.lowercase()
        val checksum = createChecksum(hrp, as5Bit)
        val combined = ByteArray(as5Bit.size + checksum.size)
        as5Bit.copyInto(combined)
        checksum.copyInto(combined, startIndex = 0, destinationOffset = as5Bit.size)

        val sb = StringBuilder(hrp.length + 1 + combined.size)
        sb.append(hrp)
        sb.append('1')
        for (b in combined) {
            sb.append(CHARSET[b.toInt()])
        }
        return sb.toString()
    }

    private fun decode(
        bech32: String,
        noChecksum: Boolean = false
    ): Triple<String, ByteArray, Encoding> {
        require(bech32.lowercase() == bech32 || bech32.uppercase() == bech32) { "mixed case strings are not valid bech32" }
        bech32.forEach { require(it.code in 33..126) { "invalid character " } }
        val input = bech32.lowercase()
        val pos = input.lastIndexOf('1')
        val hrp = input.take(pos)
        require(hrp.length in 1..83) { "hrp must contain 1 to 83 characters" }
        val data = ByteArray(input.length - pos - 1) { 0 }
        for (i in 0..data.lastIndex) data[i] = map[input[pos + 1 + i].code]
        return if (noChecksum) {
            Triple(hrp, data, Encoding.Bech32WithoutChecksum)
        } else {
            val encoding = when (polymod(expandHrp(hrp) + data)) {
                Encoding.Bech32.constant -> Encoding.Bech32
                else -> throw IllegalArgumentException("invalid checksum for $bech32")
            }
            Triple(hrp, data.dropLast(6).toByteArray(), encoding)
        }
    }

    fun decodeBytes(
        bech32: String,
        noChecksum: Boolean = false
    ): Triple<String, ByteArray, Encoding> {
        val (hrp, int5s, encoding) = decode(bech32, noChecksum)
        return Triple(hrp, convert5bitDataToBytes(int5s, 0), encoding)
    }

    private fun convertTo5BitData(data: ByteArray): ByteArray {
        val output = ArrayList<Byte>()
        var buffer = 0L
        var count = 0
        data.forEach { b ->
            buffer = (buffer shl 8) or (b.toLong() and 0xff)
            count += 8
            while (count >= 5) {
                output.add(((buffer shr (count - 5)) and 31).toByte())
                count -= 5
            }
        }
        if (count > 0) output.add(((buffer shl (5 - count)) and 31).toByte())

        return output.toByteArray()
    }

    private fun convert5bitDataToBytes(input: ByteArray, offset: Int): ByteArray {
        var buffer = 0L
        val output = ArrayList<Byte>()
        var count = 0
        for (i in offset..input.lastIndex) {
            val b = input[i]
            buffer = (buffer shl 5) or (b.toLong() and 31)
            count += 5
            while (count >= 8) {
                output.add(((buffer shr (count - 8)) and 0xff).toByte())
                count -= 8
            }
        }
        require(count <= 4) { "Zero-padding of more than 4 bits" }
        require((buffer and ((1L shl count) - 1L)) == 0L) { "Non-zero padding in 8-to-5 conversion" }
        return output.toByteArray()
    }

}
