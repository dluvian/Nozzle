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
)

fun getDefaultRelays(): List<String> = defaultRelays

// All relay respectors in my local data base
private val defaultPubkeys = listOf(
    // dluvian
    "e4336cd525df79fa4d3af364fd9600d4b10dce4215aa4c33ed77ea0842344b10",
    // fiatjaf
    "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d",
    // mikedilger
    "ee11a5dff40c19a555f41fe42b48f00e618c91225622ae37b6c2bb67b76c4e49",
    // hodlbod
    "97c70a44366a6535c145b333f973ea86dfdc2d7a99da618c40c64705ad98e322",
    // mieku
    "4c800257a588a82849d049817c2bdaad984b25a45ad9f6dad66e47d3b47e3b2f",
    // thegrinder
    "6e75f7972397ca3295e0f4ca0fbc6eb9cc79be85bafdd56bd378220ca8eee74e",
    // drfred
    "be49045474d8234adbd38dff67bbb9ae2a6d0696bf809e44e9cd12aac0ea6318"
    // People I'd like to add once they respect relays:
    // - jack
    // - snowden
    // - odell
    // - lynalden
    // - jb55
    // - kieran
    // - nostreport
    // - gladstein
    // - walker
    // - unclebob
    // - gigi
    // - karnage
    // - pablo
    // - prestonpysh
    // - stellainforest
)

fun getDefaultPubkeys(): List<String> = defaultPubkeys
