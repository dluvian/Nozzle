package com.dluvian.nozzle.ui.app.views.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.nostr.nip05.INip05Resolver
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.URI
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNprofileStr
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.utils.HashtagUtils
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val nip05Resolver: INip05Resolver,
    private val simpleProfileProvider: ISimpleProfileProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchViewModelState())
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            _uiState.value
        )

    var profileSearchResult: StateFlow<List<ProfileSearchResult>> = MutableStateFlow(emptyList())

    val onSearch: (String) -> Unit = local@{ input ->
        if (input.isBlank()) return@local

        val trimmed = input.trim()
        if (HashtagUtils.isHashtag(trimmed)) setFinalId(trimmed)
        else if (nip05Resolver.isNip05(trimmed)) resolveNip05(trimmed)
        else when (val nostrId = nostrStrToNostrId(nostrStr = trimmed.removePrefix(URI))) {
            is NpubNostrId,
            is NprofileNostrId,
            is NoteNostrId,
            is NeventNostrId -> setFinalId(nostrId.nostrStr)

            null -> {
                handleNameSearch(trimmed)
            }
        }

    }

    val onResetUI: () -> Unit = {
        _uiState.update {
            it.copy(
                isLoading = false,
                isInvalidNip05 = false,
                finalId = ""
            )
        }
    }

    private fun resolveNip05(nip05: String) {
        if (_uiState.value.isLoading) return
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
        }.invokeOnCompletion {
            setLoading(false)
        }
    }

    private fun handleNameSearch(name: String) {
        if (name.isBlank()) return
        if (_uiState.value.isLoading) return
        setLoading(true)

        viewModelScope.launch(context = Dispatchers.IO) {
            profileSearchResult = simpleProfileProvider
                .getSimpleProfilesFlow(nameLike = name)
                .distinctUntilChanged()
                .map { list -> list.map { ProfileSearchResult(it) } }
                .stateIn(
                    viewModelScope, SharingStarted.WhileSubscribed(SCOPE_TIMEOUT), emptyList()
                )
        }.invokeOnCompletion {
            setLoading(false)
        }
    }

    private fun setNip05Invalid() {
        _uiState.update { it.copy(isInvalidNip05 = true) }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun setFinalId(finalId: String) {
        _uiState.update { it.copy(finalId = finalId) }
    }

    companion object {
        fun provideFactory(
            nip05Resolver: INip05Resolver,
            simpleProfileProvider: ISimpleProfileProvider,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(
                        nip05Resolver = nip05Resolver,
                        simpleProfileProvider = simpleProfileProvider
                    ) as T
                }
            }
    }
}
