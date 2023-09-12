package com.dluvian.nozzle.model.nostr

sealed class NostrId

class NpubNostrId(val npub: String, val pubkeyHex: String) : NostrId()

class NprofileNostrId(val nprofile: String, val pubkeyHex: String) : NostrId()

class NoteNostrId(val note1: String) : NostrId()

class NeventNostrId(val nevent: String) : NostrId()
