package com.dluvian.nozzle.data


private val defaultRelays = listOf(
    // EU
    "wss://nostr.einundzwanzig.space",
    "wss://nostr.lu.ke",
    "wss://nostr.mom",

    // NA
    "wss://nostr.fmt.wiz.biz",

    // JP
    "wss://relay.nostr.wirednet.jp",

    "wss://nostr.mikedilger.com",
    "wss://relay.verified-nostr.com",
    "wss://bitcoiner.social",


    )

fun getDefaultRelays(): List<String> = defaultRelays
