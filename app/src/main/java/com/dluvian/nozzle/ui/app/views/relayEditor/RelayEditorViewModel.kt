package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class RelayEditorViewModelState(
    val relays: List<String> = listOf(),
)

class RelayEditorViewModel : ViewModel() {

    private val uiFlow = MutableStateFlow(RelayEditorViewModelState())
    val uiState = uiFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        uiFlow.value
    )

    companion object {
        fun provideFactory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RelayEditorViewModel() as T
                }
            }
    }
}
