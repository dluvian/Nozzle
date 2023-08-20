package com.dluvian.nozzle.ui.app.views.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.PostWithMeta

@Composable
fun ThreadRoute(
    threadViewModel: ThreadViewModel,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val thread by threadViewModel.threadState.collectAsState()
    val isRefreshing by threadViewModel.isRefreshingState.collectAsState()

    ThreadScreen(
        thread = thread,
        isRefreshing = isRefreshing,
        onPrepareReply = onPrepareReply,
        onRefreshThreadView = threadViewModel.onRefreshThreadView,
        onLike = { post ->
            threadViewModel.postCardInteractor.like(
                scope = threadViewModel.viewModelScope,
                postId = post.id,
                postPubkey = post.pubkey
            )
        },
        onShowMedia = { mediaUrl ->
            threadViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            threadViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onOpenThread = threadViewModel.onOpenThread,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToReply = onNavigateToReply,
        onNavigateToQuote = onNavigateToQuote,
        onGoBack = onGoBack,
    )
}
