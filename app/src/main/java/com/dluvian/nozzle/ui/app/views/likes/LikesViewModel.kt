package com.dluvian.nozzle.ui.app.views.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.provider.feed.ILikeFeedProvider
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.CreatedAt
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class LikesViewModel(
    private val likeFeedProvider: ILikeFeedProvider,
    reactionDao: ReactionDao,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
        _isRefreshing.value
    )

    private val paginator: IPaginator<PostWithMeta, CreatedAt> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _isRefreshing.update { bool } },
        onGetPage = { lastCreatedAt, waitForSubscription ->
            likeFeedProvider.getLikeFeedFlow(
                limit = DB_BATCH_SIZE,
                until = lastCreatedAt,
                waitForSubscription = waitForSubscription
            )
        },
        onIdentifyLastParam = { post -> post?.entity?.createdAt ?: getCurrentTimeInSeconds() }
    )

    val feed = paginator.getList()
    val numOfNewPosts = paginator.getNumOfNewItems()
    val likeCount = reactionDao.getLikeCountFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = SCOPE_TIMEOUT,
            replayExpirationMillis = 0
        ),
        initialValue = -1,
    )

    val onOpenLikes: () -> Unit = {
        paginator.refresh(waitForSubscription = false, useInitialValue = false)
    }

    val onRefresh: () -> Unit = {
        paginator.refresh(waitForSubscription = true, useInitialValue = true)
    }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    companion object {
        fun provideFactory(
            likeFeedProvider: ILikeFeedProvider,
            reactionDao: ReactionDao,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LikesViewModel(
                        likeFeedProvider = likeFeedProvider,
                        reactionDao = reactionDao,
                    ) as T
                }
            }
    }
}
