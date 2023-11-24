package com.dluvian.nozzle.ui.app.views.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.provider.feed.ILikeFeedProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class LikesViewModel(
    val clickedMediaUrlCache: IClickedMediaUrlCache,
    private val likeFeedProvider: ILikeFeedProvider
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
        _isRefreshing.value
    )

    private val paginator: IPaginator = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _isRefreshing.update { bool } }
    ) { lastCreatedAt, waitForSubscription ->
        likeFeedProvider.getLikeFeedFlow(
            limit = DB_BATCH_SIZE,
            until = lastCreatedAt,
            waitForSubscription = waitForSubscription
        )
    }

    val feed = paginator.getFeed()

    val onOpenLikes: () -> Unit = { paginator.reset() }

    val onRefresh: () -> Unit = { paginator.refresh() }

    val onLoadMore: () -> Unit = { paginator.loadMore() }


    companion object {
        fun provideFactory(
            clickedMediaUrlCache: IClickedMediaUrlCache,
            likeFeedProvider: ILikeFeedProvider,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LikesViewModel(
                        clickedMediaUrlCache = clickedMediaUrlCache,
                        likeFeedProvider = likeFeedProvider
                    ) as T
                }
            }
    }
}
