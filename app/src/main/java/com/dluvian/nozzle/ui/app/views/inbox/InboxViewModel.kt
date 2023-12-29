package com.dluvian.nozzle.ui.app.views.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.feed.IInboxFeedProvider
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.CreatedAt
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class InboxViewModel(
    private val inboxFeedProvider: IInboxFeedProvider,
    private val relayProvider: IRelayProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InboxViewModelState())
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
        _uiState.value
    )

    private val paginator: IPaginator<PostWithMeta, CreatedAt> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _uiState.update { it.copy(isRefreshing = bool) } },
        onGetPage = { lastCreatedAt, waitForSubscription ->
            inboxFeedProvider.getInboxFeedFlow(
                relays = _uiState.value.relays,
                limit = DB_BATCH_SIZE,
                until = lastCreatedAt,
                waitForSubscription = waitForSubscription
            )
        },
        onIdentifyLastParam = { post -> post?.entity?.createdAt ?: getCurrentTimeInSeconds() }
    )

    val feed = paginator.getList()
    val numOfNewPosts = paginator.getNumOfNewItems()

    val onOpenInbox: () -> Unit = { updateScreen(useInitialValue = false) }

    val onRefresh: () -> Unit = { updateScreen(useInitialValue = true) }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    private fun updateScreen(useInitialValue: Boolean) {
        _uiState.update { it.copy(relays = relayProvider.getReadRelays()) }
        paginator.refresh(waitForSubscription = true, useInitialValue = useInitialValue)
    }

    companion object {
        fun provideFactory(
            inboxFeedProvider: IInboxFeedProvider,
            relayProvider: IRelayProvider,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InboxViewModel(
                        inboxFeedProvider = inboxFeedProvider,
                        relayProvider = relayProvider
                    ) as T
                }
            }
    }
}
