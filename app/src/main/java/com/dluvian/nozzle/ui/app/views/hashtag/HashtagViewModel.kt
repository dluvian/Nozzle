package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_APPEND_BATCH_SIZE
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.MAX_APPEND_ATTEMPTS
import com.dluvian.nozzle.data.MAX_FEED_LENGTH
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.utils.*
import com.dluvian.nozzle.data.utils.HashtagUtils.removeHashtagPrefix
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


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

    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    private val failedAppendAttempts = AtomicInteger(0)

    val onOpenHashtag: (String) -> Unit = { hashtag ->
        val lowerCaseHashtag = hashtag.lowercase().removeHashtagPrefix()
        if (lowerCaseHashtag != uiState.value.feedSettings.hashtag) {
            viewModelScope.launch(context = Dispatchers.IO) {
                failedAppendAttempts.set(0)
                updateScreen(hashtag = lowerCaseHashtag)
            }
        }
    }

    val onRefresh: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            updateScreen(hashtag = _uiState.value.feedSettings.hashtag.orEmpty())
        }
    }

    private val isAppending = AtomicBoolean(false)
    val onLoadMore: () -> Unit = {
        // TODO: Use pagination API. Also in other vms
        if (!isAppending.get() && failedAppendAttempts.get() <= MAX_APPEND_ATTEMPTS) {
            isAppending.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                val currentFeed = feedState.value
                appendFeed(currentFeed = currentFeed)
                if (currentFeed.lastOrNull()?.entity?.id.orEmpty() == feedState.value.lastOrNull()?.entity?.id.orEmpty()) {
                    failedAppendAttempts.incrementAndGet()
                }
                delay(WAIT_TIME)
                isAppending.set(false)
            }
        }
    }

    private suspend fun updateScreen(hashtag: String) {
        val isRefresh = hashtag == _uiState.value.feedSettings.hashtag
        _uiState.update {
            it.copy(
                isRefreshing = true,
                feedSettings = it.feedSettings.copy(hashtag = hashtag)
            )
        }
        updateRelays()
        updateFeed(isRefresh = isRefresh)
        if (isRefresh) delay(WAIT_TIME)
        _uiState.update { it.copy(isRefreshing = false) }
    }

    private suspend fun updateFeed(isRefresh: Boolean) {
        feedState = feedProvider.getFeedFlow(
            feedSettings = _uiState.value.feedSettings,
            limit = DB_BATCH_SIZE,
            waitForSubscription = WAIT_TIME,
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
            if (isRefresh) feedState.value else emptyList(),
        )
    }

    // TODO: Append in FeedProvider to reduce duplicate code in ProvileVM and FeedVM
    private suspend fun appendFeed(currentFeed: List<PostWithMeta>) {
        _uiState.update { it.copy(isRefreshing = true) }
        feedState.value.lastOrNull()?.let { last ->
            feedState = feedProvider.getFeedFlow(
                feedSettings = _uiState.value.feedSettings,
                limit = DB_APPEND_BATCH_SIZE,
                until = last.entity.createdAt
            ).map { toAppend -> currentFeed.takeLast(MAX_FEED_LENGTH) + toAppend }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                    currentFeed,
                )
        }
        delay(WAIT_TIME)
        _uiState.update { it.copy(isRefreshing = false) }
    }


    private fun updateRelays() {
        _uiState.update { ui ->
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
