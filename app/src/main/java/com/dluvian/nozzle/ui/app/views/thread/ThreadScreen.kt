package com.dluvian.nozzle.ui.app.views.thread

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ThreadPosition
import com.dluvian.nozzle.ui.components.PullRefreshBox
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.postCard.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCard
import com.dluvian.nozzle.ui.components.postCard.PostNotFound
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun ThreadScreen(
    thread: PostThread,
    isRefreshing: Boolean,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLike: (PostWithMeta) -> Unit,
    onRefreshThreadView: () -> Unit,
    onOpenThread: (String) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onGoBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
    onNavigateToId: (String) -> Unit,
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
                onOpenThread = onOpenThread,
                onShowMedia = onShowMedia,
                onShouldShowMedia = onShouldShowMedia,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToReply = onNavigateToReply,
                onNavigateToQuote = onNavigateToQuote,
                onNavigateToId = onNavigateToId,
            )
        }
    }
    if (thread.current == null) NoPostsHint()
}

@Composable
private fun ThreadedPosts(
    thread: PostThread,
    isRefreshing: Boolean,
    onPrepareReply: (PostWithMeta) -> Unit,
    onRefresh: () -> Unit,
    onLike: (PostWithMeta) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onOpenThread: (String) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onNavigateToReply: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
    onNavigateToId: (String) -> Unit,
) {
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = thread.previous.size)
    LaunchedEffect(key1 = thread.previous.size) {
        lazyListState.scrollToItem(thread.previous.size)
    }
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            thread.current?.let {
                itemsIndexed(thread.previous) { index, post ->
                    var threadPosition = ThreadPosition.MIDDLE
                    if (index == 0) {
                        if (post.entity.replyToId != null) {
                            PostNotFound()
                        } else {
                            threadPosition = ThreadPosition.START
                        }
                    }
                    PostCard(
                        post = post,
                        onLike = { onLike(post) },
                        onPrepareReply = onPrepareReply,
                        onNavigateToThread = onOpenThread,
                        onNavigateToReply = onNavigateToReply,
                        onNavigateToQuote = onNavigateToQuote,
                        onShowMedia = onShowMedia,
                        onShouldShowMedia = onShouldShowMedia,
                        threadPosition = threadPosition,
                        onOpenProfile = onNavigateToProfile,
                        onNavigateToId = onNavigateToId,
                    )
                }
                item {
                    if (it.entity.replyToId != null && thread.previous.isEmpty()) {
                        PostNotFound()
                    }
                    val focusColor = colors.primaryVariant
                    PostCard(
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = focusColor,
                                strokeWidth = 21f,
                                start = Offset(x = 0f, y = 0f),
                                end = Offset(x = 0f, y = size.height),
                            )
                        },
                        post = it,
                        onLike = { onLike(it) },
                        onPrepareReply = onPrepareReply,
                        onNavigateToThread = onOpenThread,
                        onNavigateToReply = onNavigateToReply,
                        onNavigateToQuote = onNavigateToQuote,
                        onShowMedia = onShowMedia,
                        onShouldShowMedia = onShouldShowMedia,
                        isCurrent = true,
                        threadPosition = thread.getCurrentThreadPosition(),
                        onOpenProfile = onNavigateToProfile,
                        onNavigateToId = onNavigateToId,
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(spacing.tiny))
                    Divider()
                }
                items(thread.replies) { post ->
                    PostCard(
                        post = post,
                        onLike = { onLike(post) },
                        onPrepareReply = onPrepareReply,
                        onNavigateToThread = onOpenThread,
                        onNavigateToReply = onNavigateToReply,
                        onNavigateToQuote = onNavigateToQuote,
                        onShowMedia = onShowMedia,
                        onShouldShowMedia = onShouldShowMedia,
                        onOpenProfile = onNavigateToProfile,
                        onNavigateToId = onNavigateToId,
                    )
                }
            }
        }
    }
}
