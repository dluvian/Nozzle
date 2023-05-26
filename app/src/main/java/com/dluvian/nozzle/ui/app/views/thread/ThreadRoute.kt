package com.dluvian.nozzle.ui.app.views.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dluvian.nozzle.model.PostWithMeta

@Composable
fun ThreadRoute(
    threadViewModel: ThreadViewModel,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReply: () -> Unit,
    onGoBack: () -> Unit,
) {
    val thread by threadViewModel.threadState.collectAsState()
    val isRefreshing by threadViewModel.isRefreshingState.collectAsState()

    ThreadScreen(
        thread = thread,
        isRefreshing = isRefreshing,
        onPrepareReply = onPrepareReply,
        onRefreshThreadView = threadViewModel.onRefreshThreadView,
        onLike = threadViewModel.onLike,
        onRepost = threadViewModel.onRepost,
        onOpenThread = threadViewModel.onOpenThread,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToReply = onNavigateToReply,
        onGoBack = onGoBack,
    )
}
