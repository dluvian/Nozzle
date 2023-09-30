package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.utils.*
import com.dluvian.nozzle.model.Everyone
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class HashtagViewModelState(
    val isRefreshing: Boolean = false,
    val feedSettings: FeedSettings = FeedSettings(
        isPosts = true,
        isReplies = true,
        hashtag = null,
        authorSelection = Everyone,
        relaySelection = MultipleRelays(emptyList())
    ),
)

class HashtagViewModel(
    val clickedMediaUrlCache: IClickedMediaUrlCache,
    val postCardInteractor: IPostCardInteractor,
    private val feedProvider: IFeedProvider,
    private val relayProvider: IRelayProvider,
) : ViewModel() {
    private val uiFlow = MutableStateFlow(HashtagViewModelState())
    val uiState = uiFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
        uiFlow.value
    )

    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    val onOpenHashtag: (String) -> Unit = { hashtag ->
        val lowerCaseHashtag = hashtag.lowercase().removePrefix("#")
        if (lowerCaseHashtag != uiState.value.feedSettings.hashtag) {
            viewModelScope.launch(context = Dispatchers.IO) {
                updateScreen(hashtag = lowerCaseHashtag)
            }
        }
    }

    val onRefresh: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            updateScreen(hashtag = uiFlow.value.feedSettings.hashtag.orEmpty())
        }
    }

    val onLoadMore: () -> Unit = {
        // TODO: Use pagination API. Don't use the crap in the other view models
    }

    private suspend fun updateScreen(hashtag: String) {
        val isRefresh = hashtag == uiFlow.value.feedSettings.hashtag
        uiFlow.update {
            it.copy(
                isRefreshing = true,
                feedSettings = it.feedSettings.copy(hashtag = hashtag)
            )
        }
        updateRelays()
        updateFeed(isRefresh = isRefresh)
        if (isRefresh) delay(WAIT_TIME)
        uiFlow.update { it.copy(isRefreshing = false) }
    }

    private suspend fun updateFeed(isRefresh: Boolean) {
        feedState = feedProvider.getFeedFlow(
            feedSettings = uiFlow.value.feedSettings,
            limit = DB_BATCH_SIZE,
            waitForSubscription = WAIT_TIME,
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
            if (isRefresh) feedState.value else emptyList(),
        )
    }

    private fun updateRelays() {
        uiFlow.update { ui ->
            ui.copy(
                feedSettings = ui.feedSettings.copy(
                    relaySelection = MultipleRelays(
                        relayProvider.getReadRelays()
                    )
                )
            )
        }
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
