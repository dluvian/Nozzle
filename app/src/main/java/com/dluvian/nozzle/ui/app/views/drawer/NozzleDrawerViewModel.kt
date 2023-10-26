package com.dluvian.nozzle.ui.app.views.drawer


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.room.dao.AccountDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class NozzleDrawerViewModel(
    keyManager: IKeyManager,
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

    private val isActivating = AtomicBoolean(false)
    val onActivateAccount: (Int) -> Unit = local@{ i ->
        val active = uiState.value.allAccounts.getOrNull(i)
        if (active == null
            || active.isActive
            || !isActivating.compareAndSet(false, true)
        ) return@local

        viewModelScope.launch(context = Dispatchers.IO) {
            keyManager.activatePubkey(pubkey = active.pubkey)
        }.invokeOnCompletion {
            isActivating.set(false)
        }
    }

    private val isDeleting = AtomicBoolean(false)
    val onDeleteAccount: (Int) -> Unit = local@{ i ->
        val toDelete = uiState.value.allAccounts.getOrNull(i)
        if (toDelete == null
            || toDelete.isActive
            || !isDeleting.compareAndSet(false, true)
        ) return@local

        viewModelScope.launch(context = Dispatchers.IO) {
            keyManager.deletePubkey(pubkey = toDelete.pubkey)
        }.invokeOnCompletion {
            isDeleting.set(false)
        }
    }


    init {
        // TODO: Subscribe all accounts
        nozzleSubscriber.subscribePersonalProfile()
    }

    companion object {
        fun provideFactory(
            keyManager: IKeyManager,
            accountDao: AccountDao,
            nozzleSubscriber: INozzleSubscriber
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NozzleDrawerViewModel(
                        keyManager = keyManager,
                        accountDao = accountDao,
                        nozzleSubscriber = nozzleSubscriber
                    ) as T
                }
            }
    }
}
