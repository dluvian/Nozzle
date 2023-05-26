package com.dluvian.nozzle.data


private val defaultRelays = listOf(
    "wss://nos.lol",
    "wss://nostr.einundzwanzig.space",
    "wss://nostr-pub.wellorder.net",
)

fun getDefaultRelays(): List<String> = defaultRelays
