package com.dluvian.nozzle.ui.app.views.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.nostr.nip05.INip05Resolver
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.URI
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNprofileStr
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.data.utils.HashtagUtils
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "SearchViewModel"

class SearchViewModel(private val nip05Resolver: INip05Resolver) : ViewModel() {

    private val viewModelState = MutableStateFlow(SearchViewModelState())

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            viewModelState.value
        )

    val onSearch: () -> Unit = {
        if (uiState.value.input.isNotBlank()) {
            val trimmed = uiState.value.input.trim().removePrefix(URI)
            if (HashtagUtils.isHashtag(trimmed)) setFinalId(trimmed)
            else if (nip05Resolver.isNip05(trimmed)) resolveNip05(trimmed)
            else when (val nostrId = nostrStrToNostrId(nostrStr = trimmed)) {
                is NpubNostrId,
                is NprofileNostrId,
                is NoteNostrId,
                is NeventNostrId -> setFinalId(nostrId.nostrStr)

                null -> {
                    Log.i(TAG, "Failed to resolve $trimmed")
                    setNostrIdInvalid()
                }
            }

        }
    }

    val onChangeInput: (String) -> Unit = { input ->
        uiState.value.let {
            viewModelState.update {
                it.copy(
                    input = input,
                    isInvalidNip05 = false,
                    isInvalidNostrId = false
                )
            }
        }
    }

    val onResetUI: () -> Unit = {
        viewModelState.update {
            it.copy(
                input = "",
                finalId = "",
                isLoading = false,
                isInvalidNostrId = false,
                isInvalidNip05 = false,
            )
        }
    }

    private fun resolveNip05(nip05: String) {
        if (viewModelState.value.isLoading) return
        setLoading(true)
        viewModelScope.launch(context = Dispatchers.IO) {
            val result = nip05Resolver.resolve(nip05)
            if (result != null) {
                val nprofile = createNprofileStr(pubkey = result.pubkey, relays = result.relays)
                if (nprofile != null) setFinalId(nprofile)
                else setNip05Invalid()
            } else {
                setNip05Invalid()
            }
            setLoading(false)
        }
    }

    private fun setNostrIdInvalid() {
        viewModelState.update { it.copy(isInvalidNostrId = true) }
    }

    private fun setNip05Invalid() {
        viewModelState.update { it.copy(isInvalidNip05 = true) }
    }

    private fun setFinalId(finalId: String) {
        viewModelState.update { it.copy(finalId = finalId) }
    }

    private fun setLoading(isLoading: Boolean) {
        viewModelState.update { it.copy(isLoading = isLoading) }
    }

    companion object {
        fun provideFactory(nip05Resolver: INip05Resolver): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(nip05Resolver = nip05Resolver) as T
                }
            }
    }
}
