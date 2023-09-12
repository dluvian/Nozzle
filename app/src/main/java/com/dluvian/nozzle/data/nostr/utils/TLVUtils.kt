package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.model.nostr.TLVAuthor
import com.dluvian.nozzle.model.nostr.TLVDefault
import com.dluvian.nozzle.model.nostr.TLVEntry
import com.dluvian.nozzle.model.nostr.TLVKind
import com.dluvian.nozzle.model.nostr.TLVRelay

object TLVUtils {
    const val TLV_DEFAULT: Byte = 0
    const val TLV_RELAY: Byte = 1
    const val TLV_AUTHOR: Byte = 2
    const val TLV_KIND: Byte = 3

    fun readTLVEntries(data: ByteArray): Result<List<TLVEntry>> {
        val result = mutableListOf<TLVEntry>()
        var currentData = data

        while (currentData.isNotEmpty()) {
            if (currentData.size < 2) {
                return Result.failure(IllegalArgumentException("TLV entry is too short"))
            }
            val type = currentData[0]
            val length = currentData[1].toInt()
            val end = length + 2
            if (end > currentData.size) {
                return Result.failure(IllegalArgumentException("L length is too long"))
            }
            val value = currentData.copyOfRange(2, end)
            val entry = when (type) {
                TLV_DEFAULT -> Result.success(TLVDefault(value = value))
                TLV_RELAY -> Result.success(TLVRelay(value = value))
                TLV_AUTHOR -> Result.success(TLVAuthor(value = value))
                TLV_KIND -> Result.success(TLVKind(value = value))
                else -> Result.failure(IllegalArgumentException("$type is an unknown type"))
            }
            entry.onSuccess { result.add(it) }
            if (entry.isFailure) {
                return Result.failure(entry.exceptionOrNull() ?: Exception())
            }
            currentData = currentData.copyOfRange(end, currentData.size)
        }

        return Result.success(result)
    }

    fun createTLVDefaultBytes(value: ByteArray) = createTLVBytes(type = TLV_DEFAULT, value = value)
    fun createTLVRelayBytes(value: ByteArray) = createTLVBytes(type = TLV_RELAY, value = value)
    fun createTLVAuthorBytes(value: ByteArray) = createTLVBytes(type = TLV_AUTHOR, value = value)
    fun createTLVKindBytes(value: Byte) =
        createTLVBytes(type = TLV_KIND, value = byteArrayOf(value))

    private fun createTLVBytes(type: Byte, value: ByteArray): ByteArray {
        val length = value.size.toByte()
        return byteArrayOf(type, length) + value
    }
}

