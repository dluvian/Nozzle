package com.dluvian.nozzle.ui.app.views.drawer


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.room.dao.AccountDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn

class NozzleDrawerViewModel(
    accountDao: AccountDao,
    nozzleSubscriber: INozzleSubscriber,
) : ViewModel() {

    val uiState = accountDao.listAccountsFlow()
        .distinctUntilChanged()
        .mapNotNull { accounts ->
            NozzleDrawerViewModelState.from(accounts = accounts)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            NozzleDrawerViewModelState()
        )

    init {
        // TODO: Subscribe all accounts
        nozzleSubscriber.subscribePersonalProfile()
    }

    companion object {
        fun provideFactory(
            accountDao: AccountDao,
            nozzleSubscriber: INozzleSubscriber
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NozzleDrawerViewModel(
                        accountDao = accountDao,
                        nozzleSubscriber = nozzleSubscriber
                    ) as T
                }
            }
    }
}
