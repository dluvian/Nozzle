package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.nostr.utils.KeyUtils.derivePubkey
import com.dluvian.nozzle.data.nostr.utils.KeyUtils.generatePrivkey
import com.dluvian.nozzle.data.utils.isHex
import org.junit.Test

internal class KeysUtilsTest {
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
}