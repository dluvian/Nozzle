package com.dluvian.nozzle.ui.app.views.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.nostr.nip05.INip05Resolver
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNprofileStr
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.provider.feed.ISearchFeedProvider
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.utils.HashtagUtils
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.feedFilter.ReadRelays
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val nip05Resolver: INip05Resolver,
    private val simpleProfileProvider: ISimpleProfileProvider,
    private val searchFeedProvider: ISearchFeedProvider,
    private val nozzleSubscriber: INozzleSubscriber,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchViewModelState())
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            _uiState.value
        )

    var profileSearchResult: StateFlow<List<SimpleProfile>> = MutableStateFlow(emptyList())
    var postSearchResult: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    private var searchJob: Job? = null
    val onTypeSearch: (String) -> Unit = { input ->
        setLoading(true)
        searchJob?.cancel()
        searchJob = viewModelScope.launch(context = Dispatchers.IO) {
            handleSearch(searchString = input.trim(), searchType = uiState.value.searchType)
        }
        searchJob?.invokeOnCompletion { setLoading(false) }
    }

    val onManualSearch: (String) -> Unit = local@{ input ->
        if (input.isBlank()) return@local

        val trimmed = input.trim()
        if (HashtagUtils.isHashtag(trimmed)) setFinalId(trimmed)
        else if (nip05Resolver.isNip05(trimmed)) resolveNip05(trimmed)
        else when (val nostrId =
            EncodingUtils.nostrStrToNostrId(nostrStr = trimmed.removePrefix(EncodingUtils.URI))) {
            is NpubNostrId,
            is NprofileNostrId,
            is NoteNostrId,
            is NeventNostrId -> setFinalId(nostrId.nostrStr)

            null -> {
                handleSearch(searchString = trimmed, searchType = uiState.value.searchType)
            }
        }
    }

    val onChangeSearchType: (SearchType) -> Unit = { type ->
        _uiState.update { it.copy(searchType = type) }
    }

    private var subJob: Job? = null
    val onSubscribeUnknownContacts: () -> Unit = {
        subJob?.cancel()
        subJob = viewModelScope.launch(Dispatchers.IO) {
            nozzleSubscriber.subscribeUnknownContacts(ReadRelays)
        }
    }

    val onResetUI: () -> Unit = {
        _uiState.update {
            it.copy(
                isLoading = false,
                isInvalidNip05 = false,
                finalId = "",
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

    private fun handleSearch(searchString: String, searchType: SearchType) {
        when (searchType) {
            SearchType.PEOPLE -> handleNameSearch(name = searchString)
            SearchType.NOTES -> handleNoteSearch(searchString = searchString)
        }
    }

    private var nameSearchJob: Job? = null
    private fun handleNameSearch(name: String) {
        setLoading(true)
        nameSearchJob?.cancel()
        nameSearchJob = viewModelScope.launch(context = Dispatchers.IO) {
            profileSearchResult = simpleProfileProvider
                .getSimpleProfilesFlow(nameLike = name)
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(SCOPE_TIMEOUT),
                    profileSearchResult.value
                )
        }
        nameSearchJob?.invokeOnCompletion { setLoading(false) }
    }

    private var noteSearchJob: Job? = null
    private fun handleNoteSearch(searchString: String) {
        setLoading(true)
        noteSearchJob?.cancel()
        noteSearchJob = viewModelScope.launch(context = Dispatchers.IO) {
            postSearchResult = searchFeedProvider
                .getSearchFeedFlow(searchString = searchString)
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(SCOPE_TIMEOUT),
                    postSearchResult.value
                )
        }
        noteSearchJob?.invokeOnCompletion { setLoading(false) }
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
            searchFeedProvider: ISearchFeedProvider,
            nozzleSubscriber: INozzleSubscriber,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(
                        nip05Resolver = nip05Resolver,
                        simpleProfileProvider = simpleProfileProvider,
                        searchFeedProvider = searchFeedProvider,
                        nozzleSubscriber = nozzleSubscriber
                    ) as T
                }
            }
    }
}
