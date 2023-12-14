package com.dluvian.nozzle.ui.app.views.thread

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.postIdToNostrId
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.model.PostThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "ThreadViewModel"

class ThreadViewModel(
    val postCardInteractor: IPostCardInteractor,
    private val threadProvider: IThreadProvider,
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

    // TODO: Why is this called multiple times?
    // TODO: Make this more readable
    private val isSettingThread = AtomicBoolean(false)
    val onOpenThread: (String) -> Unit = local@{ postId ->
        val hexId = postIdToNostrId(postId)?.hex ?: postId
        if (isSettingThread.get() || hexId == threadState.value.current?.entity?.id) return@local

        isSettingThread.set(true)
        isRefreshingFlow.update { true }
        Log.i(TAG, "Open thread of post $postId")
        threadState = MutableStateFlow(PostThread.createEmpty())
        findingParentsProcess?.cancel(CancellationException("Opened another thread"))
        currentPostId = postId
        viewModelScope.launch(context = Dispatchers.IO) {
            updateScreen(postId = postId)
            isRefreshingFlow.update { false }
        }.invokeOnCompletion {
            isSettingThread.set(false)
        }
    }

    val onRefreshThreadView: () -> Unit = local@{
        if (isSettingThread.get()) return@local
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

    private var findingParentsProcess: Job? = null
    private val isFindingPrevious = AtomicBoolean(false)
    val onFindPrevious: () -> Unit = local@{
        if (!isFindingPrevious.compareAndSet(false, true)) return@local

        val earliestPost = threadState.value.previous.firstOrNull() ?: threadState.value.current
        if (earliestPost == null || earliestPost.entity.replyToId == null) {
            isFindingPrevious.set(false)
            return@local
        }

        findingParentsProcess = viewModelScope.launch(context = Dispatchers.IO) {
            threadProvider.findParents(earliestPost = earliestPost)
        }
        findingParentsProcess?.invokeOnCompletion {
            Log.i(TAG, "Finding parent process completed: error=${it?.localizedMessage}")
            isFindingPrevious.set(false)
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
            waitForSubscription = waitForSubscription
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
            initValue,
        )
    }

    companion object {
        fun provideFactory(
            threadProvider: IThreadProvider,
            postCardInteractor: IPostCardInteractor,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThreadViewModel(
                    postCardInteractor = postCardInteractor,
                    threadProvider = threadProvider,
                ) as T
            }
        }
    }
}
