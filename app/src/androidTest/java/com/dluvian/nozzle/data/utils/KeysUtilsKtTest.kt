package com.dluvian.nozzle.data.utils

import org.junit.Test

internal class KeysUtilsKtTest {

    @Test
    fun generatePrivkeyGenerates64HexChars() {
        val privkey = generatePrivkey()

        assert(privkey.isHex())
        assert(privkey.length == 64)
    }

    @Test
    fun derivePubkeyDerivesCorrectPubkey() {
        val privkey = "a0244a7a2cf9172532d100c424ed5737c688a71f4e6c6e1b559d45f2684d1e93"
        val expectedPubkey = "8f83f7586cf53ae6fc4e78dc014860132a51cd1e4bdb27866baccf7acc090530"

        val derived = derivePubkey(privkey)

        assert(derived == expectedPubkey)
    }

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
    fun noteIdToHexConvertsNoteIdToHex() {
        val noteId = "note1xy9fv8ntag53ts5t7967tehc6edrvgpdutx93c9g9vrr4zpqm46slh9vlf"
        val expectedHex = "310a961e6bea2915c28bf175e5e6f8d65a36202de2cc58e0a82b063a8820dd75"

        val result = noteIdToHex(noteId)

        assert(result.isSuccess)
        assert(result.getOrNull() == expectedHex)
    }

    @Test
    fun noteIdToHexFailsOnInvalidNoteId() {
        val noteId = "note1cx5v7vvvdgdq7f76fcszy9wvrnl0ulehkkudzy"

        val result = noteIdToHex(noteId)

        assert(result.isFailure)
    }

}
