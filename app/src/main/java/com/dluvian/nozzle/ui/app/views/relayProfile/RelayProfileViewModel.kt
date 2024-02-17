package com.dluvian.nozzle.ui.app.views.relayProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.nip65Updater.INip65Updater
import com.dluvian.nozzle.data.provider.IRelayProfileProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.model.ItemWithOnlineStatus
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.Waiting
import com.dluvian.nozzle.model.relay.RelayProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RelayProfileViewModel(
    private val relayProfileProvider: IRelayProfileProvider,
    private val relayProvider: IRelayProvider,
    private val nip65Updater: INip65Updater,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RelayProfileViewModelState())
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        _uiState.value
    )

    private val defaultValue: ItemWithOnlineStatus<RelayProfile?> =
        ItemWithOnlineStatus(item = null, onlineStatus = Waiting)

    private var _relayProfile: StateFlow<ItemWithOnlineStatus<RelayProfile?>> =
        MutableStateFlow(defaultValue)
    val relayProfile get() = _relayProfile

    val onOpenRelayProfile: (Relay) -> Unit = local@{ relay ->
        if (_uiState.value.relay == relay) return@local

        _uiState.update { it.copy(isRefreshing = true, relay = relay) }
        viewModelScope.launch(Dispatchers.IO) {
            _relayProfile = relayProfileProvider.getRelayProfile(relayUrl = relay).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                defaultValue
            )
            updateNip65Info(relay = relay)
            delay(WAIT_TIME)
        }.invokeOnCompletion {
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    val onRefresh: () -> Unit = local@{
        if (_uiState.value.isRefreshing) return@local

        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            relayProfileProvider.update(relayUrl = _uiState.value.relay)
            updateNip65Info(relay = _uiState.value.relay)
            delay(WAIT_TIME)
        }.invokeOnCompletion {
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    val onAddToNip65: () -> Unit = {
        _uiState.update { it.copy(isUpdatingNip65 = true, isAddableToNip65 = false) }
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRelays = relayProvider.getNip65Relays() + Nip65Relay(
                url = _uiState.value.relay,
                isRead = true,
                isWrite = true
            )
            nip65Updater.publishAndSaveInDb(nip65Relays = updatedRelays)
            delay(WAIT_TIME)
        }.invokeOnCompletion {
            _uiState.update { it.copy(isUpdatingNip65 = false) }
        }
    }

    private fun updateNip65Info(relay: Relay) {
        val myRelays = relayProvider.getNip65Relays()
        val isAddable = myRelays.size < MAX_RELAYS && myRelays.none { it.url == relay }
        _uiState.update { it.copy(isAddableToNip65 = isAddable) }
    }

    companion object {
        fun provideFactory(
            relayProfileProvider: IRelayProfileProvider,
            relayProvider: IRelayProvider,
            nip65Updater: INip65Updater,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RelayProfileViewModel(
                        relayProfileProvider = relayProfileProvider,
                        relayProvider = relayProvider,
                        nip65Updater = nip65Updater
                    ) as T
                }
            }
    }
}
