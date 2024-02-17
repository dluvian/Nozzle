package com.dluvian.nozzle.ui.app.views.relayProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.relay.RelayProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class RelayProfileViewModel : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        _isRefreshing.value
    )

    var relayProfile: StateFlow<RelayProfile> = MutableStateFlow(RelayProfile())

    val onOpenRelayProfile: (Relay) -> Unit = {
        TODO()
    }

    val onRefresh: () -> Unit = {
        _isRefreshing.update { true }
        TODO()
        _isRefreshing.update { false }
    }


    companion object {
        fun provideFactory(
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RelayProfileViewModel(
                    ) as T
                }
            }
    }
}