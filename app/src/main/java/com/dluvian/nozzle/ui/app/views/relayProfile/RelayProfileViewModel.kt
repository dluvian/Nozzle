package com.dluvian.nozzle.ui.app.views.relayProfile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.provider.IRelayProfileProvider
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
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        _isRefreshing.value
    )

    private val defaultValue: ItemWithOnlineStatus<RelayProfile?> =
        ItemWithOnlineStatus(item = null, onlineStatus = Waiting)

    private var _relayProfile: StateFlow<ItemWithOnlineStatus<RelayProfile?>> =
        MutableStateFlow(defaultValue)
    val relayProfile get() = _relayProfile

    val currentRelay = mutableStateOf("")

    val onOpenRelayProfile: (Relay) -> Unit = { relay ->
        _isRefreshing.update { true }
        val isSame = currentRelay.value == relay
        viewModelScope.launch(Dispatchers.IO) {
            currentRelay.value = relay
            _relayProfile = relayProfileProvider.getRelayProfile(relayUrl = relay).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                if (isSame) relayProfile.value else defaultValue
            )
            delay(WAIT_TIME)
        }.invokeOnCompletion {
            _isRefreshing.update { false }
        }
    }

    val onRefresh: () -> Unit = {
        _isRefreshing.update { true }
        viewModelScope.launch(Dispatchers.IO) {
            relayProfileProvider.update(relayUrl = currentRelay.value)
            delay(WAIT_TIME)
        }.invokeOnCompletion {
            _isRefreshing.update { false }
        }
    }

    companion object {
        fun provideFactory(
            relayProfileProvider: IRelayProfileProvider
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RelayProfileViewModel(
                        relayProfileProvider = relayProfileProvider
                    ) as T
                }
            }
    }
}
