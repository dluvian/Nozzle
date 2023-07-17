package com.dluvian.nozzle.ui.app.views.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.utils.noteIdToHex
import com.dluvian.nozzle.data.utils.npubToHex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val TAG = "SearchViewModel"

data class SearchViewModelState(
    val input: String = "",
    val isInvalid: Boolean = false,
)

class SearchViewModel : ViewModel() {

    private val viewModelState = MutableStateFlow(SearchViewModelState())

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value
        )

    init {
        Log.i(TAG, "Initialize SearchViewModel")
    }

    val onValidateAndNavigateToDestination: ((String) -> Unit, (String, String?) -> Unit) -> Unit =
        { onNavigateToProfile, onNavigateToThread ->
            uiState.value.input.let { input ->
                val trimmed = input.trim()
                if (trimmed.startsWith("npub1")) {
                    val npub = npubToHex(trimmed)
                    npub.onSuccess { onNavigateToProfile(it) }
                    npub.onFailure { setUIInvalid() }
                } else if (trimmed.startsWith("note1")) {
                    val noteId = noteIdToHex(trimmed)
                    noteId.onSuccess { id -> onNavigateToThread(id, null) }
                    noteId.onFailure { setUIInvalid() }
                } else {
                    setUIInvalid()
                }
            }
        }

    val onChangeInput: (String) -> Unit = { input ->
        uiState.value.let {
            viewModelState.update { it.copy(input = input) }
        }
    }

    val onResetUI: () -> Unit = {
        viewModelState.update {
            it.copy(input = "", isInvalid = false)
        }
    }

    private fun setUIInvalid() {
        viewModelState.update { it.copy(isInvalid = true) }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel() as T
                }
            }
    }
}
