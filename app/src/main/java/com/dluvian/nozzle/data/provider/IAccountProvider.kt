package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.room.helper.extended.AccountEntityExtended
import kotlinx.coroutines.flow.StateFlow

interface IAccountProvider {
    fun listAccounts(): List<AccountEntityExtended>
    fun getAccountsFlow(): StateFlow<List<AccountEntityExtended>>
}
