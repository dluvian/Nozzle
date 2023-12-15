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

private val defaultPubkeys = listOf(
    // dluvian - Nozzle main dev
    "e4336cd525df79fa4d3af364fd9600d4b10dce4215aa4c33ed77ea0842344b10",
    // fiatjaf - Nostr CEO
    "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d",
    // jack - Nostr sugar daddy
    "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2",
    // nostreport - Curated lists of nostr posts
    "2edbcea694d164629854a52583458fd6d965b161e3c48b57d3aff01940558884",
    // odell - Humble guy who's stacking sats
    "04c915daefee38317fa734444acee390a8269fe5810b2241e5e6dd343dfbecc9",
    // yegorpetrov - Nostr guy
    "1577e4599dd10c863498fe3c20bd82aafaf829a595ce83c5cf8ac3463531b09b",
    // karnage - Nostr designer
    "1bc70a0148b3f316da33fe3c89f23e3e71ac4ff998027ec712b905cd24f6a411",
    // unclebob - Clean coder, pilot and nostr OG
    "2ef93f01cd2493e04235a6b87b10d3c4a74e2a7eb7c3caf168268f6af73314b5",
    // hodlbod - Coracle dev
    "97c70a44366a6535c145b333f973ea86dfdc2d7a99da618c40c64705ad98e322",
    // mikedilger - Gossip dev and relay respector
    "ee11a5dff40c19a555f41fe42b48f00e618c91225622ae37b6c2bb67b76c4e49",
    // pablof7z - Nostr wonder child
    "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
    // kieran - Snort dev
    "63fe6318dc58583cfe16810f86dd09e18bfd76aabc24a0081ce2856f330504ed",
    // gladstein - Freedom fighter
    "58c741aa630c2da35a56a77c1d05381908bd10504fdd2d8b43f725efa6d23196",
    // snowden - Our boi
    "84dee6e676e5bb67b4ad4e042cf70cbd8681155db535942fcc6a0533858a7240",
    // gigi - Bitcoiner
    "6e468422dfb74a5738702a8823b9b28168abab8655faacb6853cd0ee15deee93",
    // yonle - Random indonesian guy and friend of Nozzle
    "347a2370900d19b4e4756221594e8bda706ae5c785de09e59e4605f91a03f49c",
)

fun getDefaultPubkeys(): List<String> = defaultPubkeys
