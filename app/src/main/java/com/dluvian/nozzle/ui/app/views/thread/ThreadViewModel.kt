package com.dluvian.nozzle.ui.app.views.thread

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.postIdToNostrId
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.data.utils.*
import com.dluvian.nozzle.model.PostThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "ThreadViewModel"
private const val WAIT_TIME = 2100L

class ThreadViewModel(
    val postCardInteractor: IPostCardInteractor,
    val clickedMediaUrlCache: IClickedMediaUrlCache,
    private val threadProvider: IThreadProvider,
    private val relayProvider: IRelayProvider,
) : ViewModel() {
    var threadState: StateFlow<PostThread> = MutableStateFlow(PostThread.createEmpty())

    private val isRefreshingFlow = MutableStateFlow(false)
    val isRefreshingState = isRefreshingFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            isRefreshingFlow.value
        )

    private var currentPostId = ""

    init {
        Log.i(TAG, "Initialize ThreadViewModel")
    }

    // TODO: Why is this called multiple times?
    // TODO: Prevent redundant subscriptions
    private val isSettingThread = AtomicBoolean(false)
    val onOpenThread: (String) -> Unit = { postId ->
        val hexId = postIdToNostrId(postId)?.hex ?: postId
        if (!isSettingThread.get() && hexId != threadState.value.current?.entity?.id) {
            isSettingThread.set(true)
            isRefreshingFlow.update { true }
            Log.i(TAG, "Open thread of post $postId")
            threadState = MutableStateFlow(PostThread.createEmpty())
            currentPostId = postId
            viewModelScope.launch(context = Dispatchers.IO) {
                updateScreen(postId = postId)
                isRefreshingFlow.update { false }
                delay(WAIT_TIME)
                isSettingThread.set(false)
            }
        }
    }

    val onRefreshThreadView: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Refresh thread view")
            isRefreshingFlow.update { true }
            updateScreen(
                postId = currentPostId,
                waitForSubscription = WAIT_TIME,
                initValue = threadState.value
            )
            delay(WAIT_TIME)
            isRefreshingFlow.update { false }
        }
    }

    private suspend fun updateScreen(
        postId: String,
        waitForSubscription: Long? = null,
        initValue: PostThread = PostThread.createEmpty()
    ) {
        Log.i(TAG, "Set new thread of post $postId")
        threadState = threadProvider.getThreadFlow(
            postId = postId,
            waitForSubscription = waitForSubscription,
            relays = relayProvider.getReadRelays()
        )
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                initValue,
            )
    }

    companion object {
        fun provideFactory(
            threadProvider: IThreadProvider,
            relayProvider: IRelayProvider,
            postCardInteractor: IPostCardInteractor,
            clickedMediaUrlCache: IClickedMediaUrlCache,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThreadViewModel(
                    postCardInteractor = postCardInteractor,
                    clickedMediaUrlCache = clickedMediaUrlCache,
                    threadProvider = threadProvider,
                    relayProvider = relayProvider,
                ) as T
            }
        }
    }
}
