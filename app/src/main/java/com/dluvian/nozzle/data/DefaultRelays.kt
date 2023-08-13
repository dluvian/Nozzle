package com.dluvian.nozzle.data


private val defaultRelays = listOf(
    // EU
    "wss://nostr.einundzwanzig.space",
    "wss://nostr.lu.ke",
    "wss://nos.lol",

    // NA
    "wss://nostr.fmt.wiz.biz",

    // JP
    "wss://relay.nostr.wirednet.jp",
)

fun getDefaultRelays(): List<String> = defaultRelays
