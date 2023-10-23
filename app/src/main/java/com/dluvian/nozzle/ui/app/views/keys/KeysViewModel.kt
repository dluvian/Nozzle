package com.dluvian.nozzle.ui.app.views.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.manager.IKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class KeysViewModel(
    private val keyManager: IKeyManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(KeysViewModelState())
    val uiState = _uiState.stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value)

    val onOpenKeys: () -> Unit = {
        _uiState.update {
            it.copy(
                npub = keyManager.getActiveNpub(),
                nsec = keyManager.getActiveNsec(),
            )
        }
    }

    companion object {
        fun provideFactory(
            keyManager: IKeyManager,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return KeysViewModel(
                        keyManager = keyManager,
                    ) as T
                }
            }
    }
}
