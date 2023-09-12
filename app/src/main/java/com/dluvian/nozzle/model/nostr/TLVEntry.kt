package com.dluvian.nozzle.model.nostr

sealed class TLVEntry(val value: ByteArray)

class TLVDefault(value: ByteArray) : TLVEntry(value = value)

class TLVRelay(value: ByteArray) : TLVEntry(value = value)

class TLVAuthor(value: ByteArray) : TLVEntry(value = value)
class TLVKind(value: ByteArray) : TLVEntry(value = value)