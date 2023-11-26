package com.dluvian.nozzle.model

import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.room.helper.extended.AccountEntityExtended

data class Account(
    val name: String,
    val pubkey: String,
    val isActive: Boolean
) {
    companion object {
        fun from(accountEntityExtended: AccountEntityExtended): Account {
            return Account(
                name = accountEntityExtended.name
                    .orEmpty()
                    .ifBlank { getShortenedNpubFromPubkey(accountEntityExtended.pubkey).orEmpty() },
                pubkey = accountEntityExtended.pubkey,
                isActive = accountEntityExtended.isActive
            )
        }
    }
}
