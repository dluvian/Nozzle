package com.dluvian.nozzle.ui.app.views.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.URI
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
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
            SharingStarted.WhileSubscribed(),
            viewModelState.value
        )

    init {
        Log.i(TAG, "Initialize SearchViewModel")
    }

    val onValidateAndNavigateToDestination: ((String) -> Unit) -> Unit = { onNavigateToId ->
        uiState.value.input.let { input ->
            val trimmed = input.trim().removePrefix(URI)
            when (val nostrId = nostrStrToNostrId(nostrStr = trimmed)) {
                is NpubNostrId,
                is NprofileNostrId,
                is NoteNostrId,
                is NeventNostrId -> onNavigateToId(nostrId.nostrStr)

                null -> {
                    Log.i(TAG, "Nostr identifier $trimmed not recognized")
                    setUIInvalid()
                }
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
