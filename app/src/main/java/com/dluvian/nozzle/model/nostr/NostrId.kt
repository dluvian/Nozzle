package com.dluvian.nozzle.model.nostr

sealed class NostrId(val nostrStr: String, val hex: String, val recommendedRelays: List<String>)

class NpubNostrId(val npub: String, val pubkeyHex: String) :
    NostrId(nostrStr = npub, hex = pubkeyHex, recommendedRelays = emptyList())

class NprofileNostrId(
    val nprofile: String,
    val pubkeyHex: String,
    val relays: List<String>
) : NostrId(nostrStr = nprofile, hex = pubkeyHex, recommendedRelays = relays)

class NoteNostrId(val note1: String, val noteIdHex: String) :
    NostrId(nostrStr = note1, hex = noteIdHex, recommendedRelays = emptyList())

class NeventNostrId(
    val nevent: String,
    val noteIdHex: String,
    val relays: List<String>
) : NostrId(nostrStr = nevent, hex = noteIdHex, recommendedRelays = relays)
