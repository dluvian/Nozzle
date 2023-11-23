package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.utils.*
import com.dluvian.nozzle.data.utils.HashtagUtils.removeHashtagPrefix
import com.dluvian.nozzle.model.MultipleRelays
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class HashtagViewModel(
    val clickedMediaUrlCache: IClickedMediaUrlCache,
    val postCardInteractor: IPostCardInteractor,
    private val feedProvider: IFeedProvider,
    private val relayProvider: IRelayProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HashtagViewModelState())
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
        _uiState.value
    )

    private val paginator: IPaginator = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _uiState.update { it.copy(isRefreshing = bool) } },
        onGetPage = { lastCreatedAt ->
            feedProvider.getFeedFlow(
                feedSettings = _uiState.value.feedSettings,
                limit = DB_BATCH_SIZE,
                until = lastCreatedAt
            )
        }
    )

    var feed = paginator.getFeed()

    val onOpenHashtag: (String) -> Unit = local@{ hashtag ->
        val lowerCaseHashtag = hashtag.lowercase().removeHashtagPrefix()
        if (lowerCaseHashtag == uiState.value.feedSettings.hashtag) return@local
        updateScreen(hashtag = lowerCaseHashtag)
    }

    val onRefresh: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            updateScreen(hashtag = _uiState.value.feedSettings.hashtag.orEmpty())
        }
    }

    val onLoadMore: () -> Unit = {
        paginator.loadMore()
    }

    private fun updateScreen(hashtag: String) {
        _uiState.update { ui ->
            ui.copy(
                feedSettings = ui.feedSettings.copy(
                    hashtag = hashtag,
                    relaySelection = MultipleRelays(
                        relayProvider.getReadRelays()
                    )
                )
            )
        }
        val isRefresh = hashtag == _uiState.value.feedSettings.hashtag
        if (isRefresh) paginator.refresh() else paginator.reset()
    }

    companion object {
        fun provideFactory(
            clickedMediaUrlCache: IClickedMediaUrlCache,
            postCardInteractor: IPostCardInteractor,
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HashtagViewModel(
                    clickedMediaUrlCache = clickedMediaUrlCache,
                    postCardInteractor = postCardInteractor,
                    feedProvider = feedProvider,
                    relayProvider = relayProvider
                ) as T
            }
        }
    }
}
