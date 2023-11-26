package com.dluvian.nozzle.ui.app.views.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun ThreadRoute(
    threadViewModel: ThreadViewModel,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val thread by threadViewModel.threadState.collectAsState()
    val isRefreshing by threadViewModel.isRefreshingState.collectAsState()

    ThreadScreen(
        thread = thread,
        isRefreshing = isRefreshing,
        postCardNavLambdas = postCardNavLambdas,
        onPrepareReply = onPrepareReply,
        onLike = { post ->
            threadViewModel.postCardInteractor.like(
                scope = threadViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onRefreshThreadView = threadViewModel.onRefreshThreadView,
        onFindPrevious = threadViewModel.onFindPrevious,
        onShowMedia = { mediaUrl ->
            threadViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            threadViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onGoBack = onGoBack,
    )
}
