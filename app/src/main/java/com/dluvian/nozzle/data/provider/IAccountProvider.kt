package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.room.helper.extended.AccountEntityExtended
import kotlinx.coroutines.flow.Flow

interface IAccountProvider {
    fun listAccounts(): List<AccountEntityExtended>

    fun getAccountsFlow(): Flow<List<AccountEntityExtended>>
}
