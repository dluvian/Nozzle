package com.dluvian.nozzle.ui.app.views.drawer


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.preferences.IDarkModePreferences
import com.dluvian.nozzle.data.provider.IAccountProvider
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.feedFilter.ReadRelays
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class NozzleDrawerViewModel(
    keyManager: IKeyManager,
    accountProvider: IAccountProvider,
    darkModePreferences: IDarkModePreferences,
    nozzleSubscriber: INozzleSubscriber,
) : ViewModel() {
    val isDarkMode = darkModePreferences.isDarkMode
    val uiState = accountProvider.getAccountsFlow()
        .mapNotNull { accounts ->
            NozzleDrawerViewModelState.from(accounts = accounts)
        }.stateIn(
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

        viewModelScope.launch(context = Dispatchers.Main) {
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

        viewModelScope.launch(context = Dispatchers.Main) {
            keyManager.deletePubkey(pubkey = toDelete.pubkey)
        }.invokeOnCompletion {
            isDeleting.set(false)
        }
    }

    val onToggleDarkMode: () -> Unit = {
        darkModePreferences.setDarkMode(!isDarkMode.value)
    }

    init {
        viewModelScope.launch(context = Dispatchers.IO) {
            nozzleSubscriber.subscribePersonalProfiles(relayFilter = ReadRelays)
        }
    }

    companion object {
        fun provideFactory(
            keyManager: IKeyManager,
            accountProvider: IAccountProvider,
            darkModePreferences: IDarkModePreferences,
            nozzleSubscriber: INozzleSubscriber
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NozzleDrawerViewModel(
                        keyManager = keyManager,
                        accountProvider = accountProvider,
                        darkModePreferences = darkModePreferences,
                        nozzleSubscriber = nozzleSubscriber
                    ) as T
                }
            }
    }
}
