package com.dluvian.nozzle.model.nostr

sealed class NostrId {
    abstract fun getHex(): String
    abstract fun getRecommendedRelays(): List<String>
}

class NpubNostrId(val npub: String, val pubkeyHex: String) : NostrId() {
    override fun getHex() = pubkeyHex
    override fun getRecommendedRelays() = emptyList<String>()
}

class NprofileNostrId(
    val nprofile: String,
    val pubkeyHex: String,
    val relays: List<String>
) : NostrId() {
    override fun getHex() = pubkeyHex
    override fun getRecommendedRelays() = relays
}

class NoteNostrId(val note1: String, val noteIdHex: String) : NostrId() {
    override fun getHex() = noteIdHex
    override fun getRecommendedRelays() = emptyList<String>()
}

class NeventNostrId(
    val nevent: String,
    val noteIdHex: String,
    val relays: List<String>
) : NostrId() {
    override fun getHex() = noteIdHex
    override fun getRecommendedRelays() = relays
}
