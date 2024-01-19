package com.dluvian.nozzle.ui.app.views.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.CreatedAt
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.feedFilter.Autopilot
import com.dluvian.nozzle.model.feedFilter.FeedFilter
import com.dluvian.nozzle.model.feedFilter.FriendCircle
import com.dluvian.nozzle.model.feedFilter.Friends
import com.dluvian.nozzle.model.feedFilter.Global
import com.dluvian.nozzle.model.feedFilter.ReadRelays
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val pubkeyProvider: IPubkeyProvider,
    private val feedProvider: IFeedProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedViewModelState())
    val uiState = _uiState.stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value)

    val filterLambdas = FeedFilterLambdas(
        onTogglePosts = {
            _uiState.update { it.copy(isPosts = !it.isPosts, isReplies = true) }
        },
        onToggleReplies = {
            _uiState.update { it.copy(isReplies = !it.isReplies, isPosts = true) }
        },
        onToggleFriends = {
            _uiState.update { it.copy(isFriends = true, isFriendCircle = false, isGlobal = false) }
        },
        onToggleFriendCircle = {
            _uiState.update { it.copy(isFriends = false, isFriendCircle = true, isGlobal = false) }
        },
        onToggleGlobal = {
            _uiState.update { it.copy(isFriends = false, isFriendCircle = false, isGlobal = true) }
        },
        onToggleAutopilot = {
            _uiState.update { it.copy(isAutopilot = true, isReadRelays = false) }
        },
        onToggleReadRelays = {
            _uiState.update { it.copy(isAutopilot = false, isReadRelays = true) }
        },
    )

    private val paginator: IPaginator<PostWithMeta, CreatedAt> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _uiState.update { it.copy(isRefreshing = bool) } },
        onGetPage = { lastCreatedAt, waitForSubscription ->
            feedProvider.getFeedFlow(
                feedFilter = getFilter(uiState = uiState.value),
                limit = DB_BATCH_SIZE,
                until = lastCreatedAt,
                waitForSubscription = waitForSubscription
            )
        },
        onIdentifyLastParam = { post -> post?.entity?.createdAt ?: getCurrentTimeInSeconds() }
    )

    val feed = paginator.getList()
    val numOfNewPosts = paginator.getNumOfNewItems()

    private var isInit = true
    val pubkeyState = pubkeyProvider.getActivePubkeyStateFlow()
        .onEach local@{
            if (it.isEmpty()) return@local
            paginator.refresh(waitForSubscription = isInit, useInitialValue = false)
            isInit = false
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val onRefresh: () -> Unit = { refresh() }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    private fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            paginator.refresh(waitForSubscription = true, useInitialValue = true)
        }
    }

    private fun getFilter(uiState: FeedViewModelState): FeedFilter {
        return FeedFilter(
            isPosts = uiState.isPosts,
            isReplies = uiState.isReplies,
            hashtag = null,
            authorFilter = if (uiState.isFriendCircle) FriendCircle
            else if (uiState.isGlobal) Global
            else Friends,
            relayFilter = if (uiState.isReadRelays) ReadRelays else Autopilot
        )
    }

    companion object {
        fun provideFactory(
            pubkeyProvider: IPubkeyProvider,
            feedProvider: IFeedProvider,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(
                    pubkeyProvider = pubkeyProvider,
                    feedProvider = feedProvider,
                ) as T
            }
        }
    }
}
