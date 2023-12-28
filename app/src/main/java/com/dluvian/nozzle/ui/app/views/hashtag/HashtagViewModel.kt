package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.utils.HashtagUtils.removeHashtagPrefix
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.CreatedAt
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


class HashtagViewModel(
    private val feedProvider: IFeedProvider,
    private val relayProvider: IRelayProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HashtagViewModelState())
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
        _uiState.value
    )

    private val paginator: IPaginator<PostWithMeta, CreatedAt> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _uiState.update { it.copy(isRefreshing = bool) } },
        onGetPage = { lastCreatedAt, waitForSubscription ->
            feedProvider.getFeedFlow(
                feedSettings = _uiState.value.feedSettings,
                limit = DB_BATCH_SIZE,
                until = lastCreatedAt,
                waitForSubscription = waitForSubscription
            )
        },
        onIdentifyLastParam = { post -> post?.entity?.createdAt ?: getCurrentTimeInSeconds() }
    )

    val feed = paginator.getList()
    val numOfNewPosts = paginator.getNumOfNewItems()

    val onOpenHashtag: (String) -> Unit = local@{ hashtag ->
        val lowerCaseHashtag = hashtag.lowercase().removeHashtagPrefix()
        if (lowerCaseHashtag == uiState.value.feedSettings.hashtag) return@local
        updateScreen(hashtag = lowerCaseHashtag)
    }

    val onRefresh: () -> Unit = {
        updateScreen(hashtag = _uiState.value.feedSettings.hashtag.orEmpty())
    }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    private fun updateScreen(hashtag: String) {
        val isSameHashtag = hashtag == uiState.value.feedSettings.hashtag
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
        paginator.refresh(waitForSubscription = isSameHashtag, useInitialValue = isSameHashtag)
    }

    companion object {
        fun provideFactory(
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HashtagViewModel(
                    feedProvider = feedProvider,
                    relayProvider = relayProvider
                ) as T
            }
        }
    }
}
