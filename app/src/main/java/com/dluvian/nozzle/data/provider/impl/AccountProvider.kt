package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.provider.IAccountProvider
import com.dluvian.nozzle.data.room.dao.AccountDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AccountProvider(accountDao: AccountDao) : IAccountProvider {
    private val scope = CoroutineScope(context = Dispatchers.Default)

    private val accountsState = accountDao.listAccountsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun listAccounts() = accountsState.value

    override fun getAccountsFlow() = accountsState
}
