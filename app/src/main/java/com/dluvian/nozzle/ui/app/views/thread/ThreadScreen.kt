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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.media3.exoplayer.ExoPlayer
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ThreadPosition
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.PullRefreshBox
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCard
import com.dluvian.nozzle.ui.components.postCard.atoms.ClickToLoadMore
import com.dluvian.nozzle.ui.components.postCard.atoms.PostNotFound
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun ThreadScreen(
    thread: PostThread,
    isRefreshing: Boolean,
    videoPlayer: ExoPlayer,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onRefreshThreadView: () -> Unit,
    onFindPrevious: () -> Unit,
    onGoBack: () -> Unit,
) {
    Column {
        ReturnableTopBar(text = stringResource(id = R.string.thread), onGoBack = onGoBack)
        Column(modifier = Modifier.fillMaxSize()) {
            ThreadedPosts(
                thread = thread,
                isRefreshing = isRefreshing,
                videoPlayer = videoPlayer,
                postCardLambdas = postCardLambdas,
                onPrepareReply = onPrepareReply,
                onRefresh = onRefreshThreadView,
                onFindPrevious = onFindPrevious,
            )
        }
    }
    if (thread.current == null) NoPostsHint()
}

@Composable
private fun ThreadedPosts(
    thread: PostThread,
    isRefreshing: Boolean,
    videoPlayer: ExoPlayer,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onRefresh: () -> Unit,
    onFindPrevious: () -> Unit,
) {
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = thread.previous.size)
    val alreadyScrolled = remember(thread.current?.entity?.id) { mutableStateOf(false) }
    LaunchedEffect(key1 = thread.current?.entity?.id) {
        if (alreadyScrolled.value) return@LaunchedEffect

        lazyListState.scrollToItem(thread.previous.size)
        alreadyScrolled.value = true
    }
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            thread.current?.let {
                itemsIndexed(
                    items = thread.previous,
                    key = { _, item -> item.entity.id }) { index, post ->
                    if (index == 0) {
                        if (post.replyToPubkey != null) {
                            ClickToLoadMore(onClick = onRefresh)
                        } else if (post.entity.replyToId != null) {
                            onFindPrevious()
                            PostNotFound()
                        }
                    }
                    PostCard(
                        post = post,
                        videoPlayer = videoPlayer,
                        postCardLambdas = postCardLambdas,
                        onPrepareReply = onPrepareReply,
                        threadPosition = if (index == 0) ThreadPosition.START else ThreadPosition.MIDDLE,
                    )
                }
                item {
                    if (thread.previous.isEmpty() && it.replyToPubkey != null) {
                        ClickToLoadMore(onClick = onRefresh)
                    } else if (thread.previous.isEmpty() && it.entity.replyToId != null) {
                        onFindPrevious()
                        PostNotFound()
                    }
                    val focusColor = colors.primaryVariant
                    PostCard(
                        post = it,
                        videoPlayer = videoPlayer,
                        postCardLambdas = postCardLambdas,
                        onPrepareReply = onPrepareReply,
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = focusColor,
                                strokeWidth = 21f,
                                start = Offset(x = 0f, y = 0f),
                                end = Offset(x = 0f, y = size.height),
                            )
                        },
                        isCurrent = true,
                        threadPosition = thread.getCurrentThreadPosition(),
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(spacing.tiny))
                    Divider()
                }
                items(items = thread.replies, key = { it.entity.id }) { post ->
                    PostCard(
                        post = post,
                        videoPlayer = videoPlayer,
                        postCardLambdas = postCardLambdas,
                        onPrepareReply = onPrepareReply,
                    )
                }
            }
        }
    }
}
