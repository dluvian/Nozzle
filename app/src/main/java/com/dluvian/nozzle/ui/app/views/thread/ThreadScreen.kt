package com.dluvian.nozzle.ui.app.views.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostIds
import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ThreadPosition
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.postCard.PostCard
import com.dluvian.nozzle.ui.components.postCard.PostNotFound
import com.dluvian.nozzle.ui.theme.LightYellow
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun ThreadScreen(
    thread: PostThread,
    isRefreshing: Boolean,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLike: (String) -> Unit,
    onRepost: (String) -> Unit,
    onRefreshThreadView: () -> Unit,
    onOpenThread: (PostIds) -> Unit,
    onGoBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReply: () -> Unit,
) {
    Column {
        ReturnableTopBar(text = stringResource(id = R.string.thread), onGoBack = onGoBack)
        Column(modifier = Modifier.fillMaxSize()) {
            ThreadedPosts(
                thread = thread,
                isRefreshing = isRefreshing,
                onPrepareReply = onPrepareReply,
                onRefresh = onRefreshThreadView,
                onLike = onLike,
                onRepost = onRepost,
                onOpenThread = onOpenThread,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToReply = onNavigateToReply,
            )
        }
    }
}

@Composable
private fun ThreadedPosts(
    thread: PostThread,
    isRefreshing: Boolean,
    onPrepareReply: (PostWithMeta) -> Unit,
    onRefresh: () -> Unit,
    onLike: (String) -> Unit,
    onRepost: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onOpenThread: (PostIds) -> Unit,
    onNavigateToReply: () -> Unit,
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh,
    ) {
        val lazyListState =
            rememberLazyListState(initialFirstVisibleItemIndex = thread.previous.size)
        LaunchedEffect(key1 = thread.previous.size) {
            lazyListState.scrollToItem(thread.previous.size)
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
            thread.current?.let {
                itemsIndexed(thread.previous) { index, post ->
                    var threadPosition = ThreadPosition.MIDDLE
                    if (index == 0) {
                        if (post.replyToId != null) {
                            PostNotFound()
                        } else {
                            threadPosition = ThreadPosition.START
                        }
                    }
                    PostCard(
                        post = post,
                        onLike = onLike,
                        onRepost = onRepost,
                        onPrepareReply = onPrepareReply,
                        threadPosition = threadPosition,
                        onOpenProfile = onNavigateToProfile,
                        onNavigateToThread = onOpenThread,
                        onNavigateToReply = onNavigateToReply
                    )
                }
                item {
                    if (it.replyToId != null && thread.previous.isEmpty()) {
                        PostNotFound()
                    }
                    PostCard(
                        post = it,
                        isCurrent = true,
                        onLike = onLike,
                        onRepost = onRepost,
                        onPrepareReply = onPrepareReply,
                        modifier = Modifier.background(color = LightYellow),
                        threadPosition = thread.getCurrentThreadPosition(),
                        onOpenProfile = onNavigateToProfile,
                        onNavigateToReply = onNavigateToReply,
                        onNavigateToThread = onOpenThread,
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(spacing.tiny))
                    Divider()
                }
                items(thread.replies) { post ->
                    PostCard(
                        post = post,
                        onLike = onLike,
                        onRepost = onRepost,
                        onPrepareReply = onPrepareReply,
                        onOpenProfile = onNavigateToProfile,
                        onNavigateToThread = onOpenThread,
                        onNavigateToReply = onNavigateToReply
                    )
                }
            }
        }
    }
}
