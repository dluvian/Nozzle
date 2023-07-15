package com.dluvian.nozzle.data


private val defaultRelays = listOf(
    "wss://nostr.einundzwanzig.space",
    "wss://relay.snort.social",
    "wss://nostr.lu.ke",
    "wss://nos.lol",
)

fun getDefaultRelays(): List<String> = defaultRelays
