package com.dluvian.nozzle.ui.app.views.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.nostr.nip05.INip05Resolver
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

    val onSearch: (String) -> Unit = local@{ input ->
        val trimmed = input.trim()
        if (trimmed.isBlank()) return@local
        val results = mutableListOf<SearchResult>()
        if (HashtagUtils.isHashtag(trimmed)) {
            results.add(HashtagSearchResult(hashtag = trimmed.removePrefix("#")))
        } else if (nip05Resolver.isNip05(trimmed)) resolveNip05(trimmed) // TODO: Get profile and add to results
        else when (val nostrId = nostrStrToNostrId(nostrStr = trimmed)) {
            is NpubNostrId,
            is NprofileNostrId,
            is NoteNostrId,
            is NeventNostrId -> {
                TODO("Get ")
            }

            null -> {
                Log.i(TAG, "Failed to resolve $trimmed")
            }
        }

        // TODO: Simple name search against db

    }

    val onResetUI: () -> Unit = {
        viewModelState.update {
            it.copy(
                isLoading = false,
                isInvalidNip05 = false,
                searchResults = emptyList() // TODO: Use latest search results
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
                if (nprofile != null) TODO("Add to searchResults")
                else setNip05Invalid()
            } else {
                setNip05Invalid()
            }
            setLoading(false)
        }
    }

    private fun setNip05Invalid() {
        viewModelState.update { it.copy(isInvalidNip05 = true) }
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
