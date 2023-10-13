package com.dluvian.nozzle.ui.app.views.inbox

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
import com.dluvian.nozzle.data.provider.IInboxFeedProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class InboxViewModel(
    val clickedMediaUrlCache: IClickedMediaUrlCache,
    val postCardInteractor: IPostCardInteractor,
    private val inboxFeedProvider: IInboxFeedProvider,
    private val relayProvider: IRelayProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InboxViewModelState())
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
        _uiState.value
    )

    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    private val failedAppendAttempts = AtomicInteger(0)

    val onOpenInbox: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            failedAppendAttempts.set(0)
            updateScreen(isRefresh = false)
        }
    }

    val onRefresh: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            updateScreen(isRefresh = true)
        }
    }

    private val isAppending = AtomicBoolean(false)
    val onLoadMore: () -> Unit = local@{
        // TODO: Use pagination API. Also in other vms
        if (isAppending.get() || failedAppendAttempts.get() >= MAX_APPEND_ATTEMPTS) {
            return@local
        }
        isAppending.set(true)
        viewModelScope.launch(context = Dispatchers.IO) {
            val currentFeed = feedState.value
            appendFeed(currentFeed = currentFeed)
            if (currentFeed.lastOrNull()?.entity?.id.orEmpty() == feedState.value.lastOrNull()?.entity?.id.orEmpty()) {
                failedAppendAttempts.incrementAndGet()
            }
            delay(WAIT_TIME)
        }.invokeOnCompletion { isAppending.set(false) }
    }

    private suspend fun updateScreen(isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                isRefreshing = true,
                relays = relayProvider.getReadRelays()
            )
        }
        updateFeed(isRefresh = isRefresh)
        if (isRefresh) delay(WAIT_TIME)
        _uiState.update { it.copy(isRefreshing = false) }
    }

    private suspend fun updateFeed(isRefresh: Boolean) {
        feedState = inboxFeedProvider.getInboxFeedFlow(
            relays = uiState.value.relays,
            limit = DB_BATCH_SIZE,
            waitForSubscription = if (isRefresh) WAIT_TIME else 0L
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
            feedState = inboxFeedProvider.getInboxFeedFlow(
                relays = uiState.value.relays,
                limit = DB_APPEND_BATCH_SIZE,
                until = last.entity.createdAt
            ).map { toAppend ->
                currentFeed.takeLast(MAX_FEED_LENGTH) + toAppend
            }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                    currentFeed,
                )
        }
        delay(WAIT_TIME)
        _uiState.update { it.copy(isRefreshing = false) }
    }

    companion object {
        fun provideFactory(
            clickedMediaUrlCache: IClickedMediaUrlCache,
            postCardInteractor: IPostCardInteractor,
            inboxFeedProvider: IInboxFeedProvider,
            relayProvider: IRelayProvider,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InboxViewModel(
                        clickedMediaUrlCache = clickedMediaUrlCache,
                        postCardInteractor = postCardInteractor,
                        inboxFeedProvider = inboxFeedProvider,
                        relayProvider = relayProvider
                    ) as T
                }
            }
    }
}
