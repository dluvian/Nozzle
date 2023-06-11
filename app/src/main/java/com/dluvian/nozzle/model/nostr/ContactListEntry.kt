package com.dluvian.nozzle.model.nostr

data class ContactListEntry(
    val pubkey: String,
    val relayUrl: String = "",
)
