package com.dluvian.nozzle.ui.app.views.addAccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


class AddAccountViewModel(
    private val keyManager: IKeyManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountViewModelState())
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SCOPE_TIMEOUT),
            _uiState.value
        )

    private val isGenerating = AtomicBoolean(false)
    val onGenerateNew: () -> Unit = local@{
        if (!isGenerating.compareAndSet(false, true)) return@local

        val privkey = KeyUtils.generatePrivkey()
        val nsec = EncodingUtils.hexToNsec(privkey)
        _uiState.update { it.copy(value = nsec, isInvalid = false) }
        isGenerating.set(false)
    }

    private val isLoggingIn = AtomicBoolean(false)
    val onLogin: (String) -> Boolean = local@{ nsec ->
        if (nsec.isBlank() || !isLoggingIn.compareAndSet(false, true)) {
            return@local false
        }

        val trimmed = nsec.trim()
        val hex = if (KeyUtils.isValidHexKey(trimmed)) trimmed
        else EncodingUtils.nsecToHex(nsec.trim())
        _uiState.update { it.copy(isInvalid = hex == null) }
        if (hex == null) {
            isLoggingIn.set(false)
            return@local false
        }

        viewModelScope.launch(context = Dispatchers.IO) {
            keyManager.addPrivkey(privkey = hex)
        }.invokeOnCompletion {
            isLoggingIn.set(false)
        }

        true
    }

    val onReset: () -> Unit = {
        _uiState.update { AddAccountViewModelState() }
    }

    companion object {
        fun provideFactory(
            keyManager: IKeyManager,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddAccountViewModel(
                    keyManager = keyManager
                ) as T
            }
        }
    }
}
