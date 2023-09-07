package com.dluvian.nozzle.data.nostr.utils

import org.junit.Test

internal class EncodingUtilsTest {

    @Test
    fun hexToNpubConvertsHexPubkeyToNpub() {
        val hex = "c1a8cf318c6a1a0f27da4e202215cc1cfefe7f37b5b8d110552087b89328574a"
        val expectedNpub = "npub1cx5v7vvvdgdq7f76fcszy9wvrnl0ulehkkudzyz4yzrm3yeg2a9quvjyrg"

        val result = hexToNpub(hex)

        assert(result == expectedNpub)
    }

    @Test
    fun hexToNsecConvertsHexPrivkeyToNsec() {
        val hex = "10a0ed4d3af05dc9b6fed41e456fa7331aa946c792756ea73a4c66edb3497ada"
        val expectedNsec = "nsec1zzsw6nf67pwundh76s0y2ma8xvd2j3k8jf6kafe6f3nwmv6f0tdqyvh66t"

        val result = hexToNsec(hex)

        assert(result == expectedNsec)
    }

    @Test
    fun npubToHexConvertsNpubToHex() {
        val npub = "npub1cx5v7vvvdgdq7f76fcszy9wvrnl0ulehkkudzyz4yzrm3yeg2a9quvjyrg"
        val expectedHex = "c1a8cf318c6a1a0f27da4e202215cc1cfefe7f37b5b8d110552087b89328574a"

        val result = npubToHex(npub)

        assert(result.isSuccess)
        assert(result.getOrNull() == expectedHex)
    }

    @Test
    fun npubToHexFailsOnInvalidNpub() {
        val npub = "npub1cx5v7vvvdgdq7f76fcszy9wvrnl0ulehkkudzy"

        val result = npubToHex(npub)

        assert(result.isFailure)
    }

    @Test
    fun nsecToHexConvertsNsecToHex() {
        val nsec = "nsec1zzsw6nf67pwundh76s0y2ma8xvd2j3k8jf6kafe6f3nwmv6f0tdqyvh66t"
        val expectedHex = "10a0ed4d3af05dc9b6fed41e456fa7331aa946c792756ea73a4c66edb3497ada"

        val result = nsecToHex(nsec)

        assert(result.isSuccess)
        assert(result.getOrNull() == expectedHex)
    }

    @Test
    fun nsecToHexFailsOnInvalidNsec() {
        val nsec = "nsec1cx5v7vvvdgdq7f76fcszy9wvrnl0ulehkkudzy"

        val result = nsecToHex(nsec)

        assert(result.isFailure)
    }

    @Test
    fun note1ToHexConvertsNoteIdToHex() {
        val note1 = "note1xy9fv8ntag53ts5t7967tehc6edrvgpdutx93c9g9vrr4zpqm46slh9vlf"
        val expectedHex = "310a961e6bea2915c28bf175e5e6f8d65a36202de2cc58e0a82b063a8820dd75"

        val result = note1ToHex(note1)

        assert(result.isSuccess)
        assert(result.getOrNull() == expectedHex)
    }

    @Test
    fun note1ToHexFailsOnInvalidNoteId() {
        val note1 = "note1cx5v7vvvdgdq7f76fcszy9wvrnl0ulehkkudzy"

        val result = note1ToHex(note1)

        assert(result.isFailure)
    }

    @Test
    fun readNeventReadsNeventCorrectly() {
        val nevent =
            "nevent1qqs2ckeu45u6trzxfh5qy0e7n0lcxl0g3yjc7960pmv0xa6m4hxs9agpzamhxue69uhhyetvv9ujumn0wd68ytnzv9hxgtcpzdmhxue69uhhyetvv9ujumn0wvhxcmmv742ldw"

        val result = readNevent(nevent)

        assert(result != null)
        assert(result?.eventId == "ac5b3cad39a58c464de8023f3e9bff837de889258f174f0ed8f3775badcd02f5")
        assert(result?.relays == listOf("wss://relay.nostr.band/", "wss://relay.nos.lol"))
    }
}
