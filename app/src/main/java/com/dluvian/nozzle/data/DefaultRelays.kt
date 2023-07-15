package com.dluvian.nozzle.data


private val defaultRelays = listOf(
    "wss://relay.snort.social",
    "wss://nostr.einundzwanzig.space",
    "wss://nostr.lu.ke",
)

fun getDefaultRelays(): List<String> = defaultRelays
