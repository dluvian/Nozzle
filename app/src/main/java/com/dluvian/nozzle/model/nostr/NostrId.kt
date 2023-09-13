package com.dluvian.nozzle.model.nostr

sealed class NostrId {
    abstract fun getHex(): String
}

class NpubNostrId(val npub: String, val pubkeyHex: String) : NostrId() {
    override fun getHex() = pubkeyHex
}

class NprofileNostrId(val nprofile: String, val pubkeyHex: String) : NostrId() {
    override fun getHex() = pubkeyHex
}

class NoteNostrId(val note1: String, val noteIdHex: String) : NostrId() {
    override fun getHex() = noteIdHex
}

class NeventNostrId(val nevent: String, val noteIdHex: String) : NostrId() {
    override fun getHex() = noteIdHex
}
