package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.utils.HashtagUtils.removeHashtagPrefix
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.CreatedAt
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.feedFilter.FeedFilter
import com.dluvian.nozzle.model.feedFilter.Global
import com.dluvian.nozzle.model.feedFilter.ReadRelays
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


class HashtagViewModel(
    private val feedProvider: IFeedProvider,
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
                feedFilter = getFeedFilter(hashtag = _uiState.value.hashtag),
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
        if (lowerCaseHashtag == uiState.value.hashtag) return@local
        updateScreen(hashtag = lowerCaseHashtag)
    }

    val onRefresh: () -> Unit = {
        updateScreen(hashtag = _uiState.value.hashtag)
    }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    private fun updateScreen(hashtag: String) {
        val isSameHashtag = hashtag == uiState.value.hashtag
        _uiState.update { it.copy(hashtag = hashtag) }
        paginator.refresh(waitForSubscription = isSameHashtag, useInitialValue = isSameHashtag)
    }

    private fun getFeedFilter(hashtag: String): FeedFilter {
        return FeedFilter(
            isPosts = true,
            isReplies = true,
            hashtag = hashtag,
            authorFilter = Global,
            relayFilter = ReadRelays
        )
    }

    companion object {
        fun provideFactory(
            feedProvider: IFeedProvider,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HashtagViewModel(
                    feedProvider = feedProvider,
                ) as T
            }
        }
    }
}
