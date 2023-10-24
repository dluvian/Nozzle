package com.dluvian.nozzle.model

import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.room.helper.extended.AccountEntityExtended

data class Account(
    val name: String,
    val picture: String,
    val pubkey: String,
) {
    companion object {
        fun from(accountEntityExtended: AccountEntityExtended): Account {
            return Account(
                name = accountEntityExtended.name
                    .orEmpty()
                    .ifBlank { getShortenedNpubFromPubkey(accountEntityExtended.pubkey).orEmpty() },
                pubkey = accountEntityExtended.pubkey,
                picture = accountEntityExtended.picture.orEmpty()
            )
        }
    }
}
