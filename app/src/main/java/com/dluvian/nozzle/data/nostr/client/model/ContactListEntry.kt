package com.dluvian.nozzle.data.nostr.client.model

data class ContactListEntry(
    val pubkey: String,
    val relayUrl: String = "",
)
