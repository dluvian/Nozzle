package com.dluvian.nozzle.ui.app.views.thread

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.data.utils.*
import com.dluvian.nozzle.model.PostIds
import com.dluvian.nozzle.model.PostThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "ThreadViewModel"
private const val WAIT_TIME = 2100L

class ThreadViewModel(
    val postCardInteractor: IPostCardInteractor,
    private val threadProvider: IThreadProvider,
    private val relayProvider: IRelayProvider,
    private val clickedMediaUrlCache: IClickedMediaUrlCache,
    private val nostrSubscriber: INostrSubscriber,
) : ViewModel() {
    var threadState: StateFlow<PostThread> = MutableStateFlow(PostThread.createEmpty())

    private val isRefreshingFlow = MutableStateFlow(false)
    val isRefreshingState = isRefreshingFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            isRefreshingFlow.value
        )

    private var currentPostIds = PostIds(id = "", replyToId = null)

    init {
        Log.i(TAG, "Initialize ThreadViewModel")
    }

    // TODO: Why is this called when returning to feed? Prevent that
    // TODO: Does not work when clicking on post from profile feed from new profile
    // TODO: Prevent redundant subscriptions
    private var job: Job? = null
    val onOpenThread: (PostIds) -> Unit = { postIds ->
        Log.i(TAG, "Open thread of post ${postIds.id}")
        threadState = MutableStateFlow(PostThread.createEmpty())
        currentPostIds = postIds
        job?.let { if (it.isActive) it.cancel() }
        job = viewModelScope.launch(context = Dispatchers.IO) {
            isRefreshingFlow.update { true }
            updateScreen(postIds = postIds)
            isRefreshingFlow.update { false }
            delay(WAIT_TIME)
            renewAdditionalDataSubscription(threadState.value)
            updateCurrentPostIds(threadState.value)
        }
    }

    val onRefreshThreadView: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Refresh thread view")
            isRefreshingFlow.update { true }
            updateScreen(
                postIds = currentPostIds,
                waitForSubscription = WAIT_TIME,
                initValue = threadState.value
            )
            isRefreshingFlow.update { false }
            delay(WAIT_TIME)
            renewAdditionalDataSubscription(threadState.value)
            updateCurrentPostIds(threadState.value)
        }
    }

    // TODO: Refactor: Same in other ViewModels
    val onShowMedia: (String) -> Unit = { mediaUrl ->
        clickedMediaUrlCache.insert(mediaUrl = mediaUrl)
    }

    // TODO: Refactor: Same in other ViewModels
    val onShouldShowMedia: (String) -> Boolean = { mediaUrl ->
        clickedMediaUrlCache.contains(mediaUrl = mediaUrl)
    }

    private suspend fun updateScreen(
        postIds: PostIds,
        waitForSubscription: Long? = null,
        initValue: PostThread = PostThread.createEmpty()
    ) {
        Log.i(TAG, "Set new thread of post ${postIds.id}")
        threadState = threadProvider.getThreadFlow(
            currentPostId = postIds.id,
            replyToId = postIds.replyToId,
            waitForSubscription = waitForSubscription,
            relays = relayProvider.getPostRelays(posts = initValue.getList())
        )
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                initValue,
            )
    }

    private fun updateCurrentPostIds(thread: PostThread) {
        thread.current?.let {
            currentPostIds = PostIds(id = it.id, replyToId = it.replyToId)
        }
    }

    private fun renewAdditionalDataSubscription(thread: PostThread) {
        val posts = thread.getList()
        nostrSubscriber.unsubscribeReferencedPostsData()
        nostrSubscriber.subscribeToReferencedData(
            posts = posts,
            relays = relayProvider.getPostRelays(posts = posts),
        )
    }

    companion object {
        fun provideFactory(
            threadProvider: IThreadProvider,
            relayProvider: IRelayProvider,
            postCardInteractor: IPostCardInteractor,
            clickedMediaUrlCache: IClickedMediaUrlCache,
            nostrSubscriber: INostrSubscriber
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThreadViewModel(
                    postCardInteractor = postCardInteractor,
                    threadProvider = threadProvider,
                    relayProvider = relayProvider,
                    clickedMediaUrlCache = clickedMediaUrlCache,
                    nostrSubscriber = nostrSubscriber
                ) as T
            }
        }
    }
}
