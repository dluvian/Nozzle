package com.dluvian.nozzle.ui.app.views.drawer

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.room.helper.extended.AccountEntityExtended
import com.dluvian.nozzle.model.Account
import com.dluvian.nozzle.model.Pubkey

data class NozzleDrawerViewModelState(
    val activeAccount: Account = Account(name = "", picture = "", pubkey = ""),
    val otherAccounts: List<Account> = emptyList(),
) {
    companion object {
        fun from(
            accounts: Collection<AccountEntityExtended>,
            defaultPubkey: Pubkey
        ): NozzleDrawerViewModelState {
            val default = Account(
                name = EncodingUtils.hexToNpub(defaultPubkey),
                picture = "",
                pubkey = defaultPubkey
            )
            if (accounts.isEmpty()) return NozzleDrawerViewModelState(activeAccount = default)

            val active = accounts.find { it.isActive }?.let { Account.from(it) } ?: default
            val other = accounts.filter { !it.isActive }.map { Account.from(it) }

            return NozzleDrawerViewModelState(activeAccount = active, otherAccounts = other)
        }
    }
}
