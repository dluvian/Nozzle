package com.dluvian.nozzle.ui.app.views.drawer

import com.dluvian.nozzle.data.room.helper.extended.AccountEntityExtended
import com.dluvian.nozzle.model.Account

data class NozzleDrawerViewModelState(
    val activeAccount: Account = Account(name = "", picture = "", pubkey = "", isActive = true),
    val allAccounts: List<Account> = emptyList(),
) {
    companion object {
        fun from(accounts: Collection<AccountEntityExtended>): NozzleDrawerViewModelState? {
            if (accounts.isEmpty()) return null

            val allMapped = accounts.map(Account::from)
            val active = allMapped.find { it.isActive } ?: return null

            return NozzleDrawerViewModelState(
                activeAccount = active,
                allAccounts = allMapped
            )
        }
    }
}
