package com.dluvian.nozzle.ui.components.postCard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.model.PostIds
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.components.PullRefreshBox

@Composable
fun PostCardList(
    posts: List<PostWithMeta>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLike: (String) -> Unit,
    onQuote: (String) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToThread: (PostIds) -> Unit,
    onNavigateToReply: () -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
    onOpenProfile: ((String) -> Unit)? = null,
) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), state = lazyListState
        ) {
            itemsIndexed(posts) { index, post ->
                PostCard(
                    post = post,
                    onLike = onLike,
                    onQuote = onQuote,
                    onOpenProfile = onOpenProfile,
                    onShowMedia = onShowMedia,
                    onShouldShowMedia = onShouldShowMedia,
                    onPrepareReply = onPrepareReply,
                    onNavigateToThread = onNavigateToThread,
                    onNavigateToReply = onNavigateToReply,
                )
                // Append the next batch when only 9 more posts are left to be shown
                if (index > 0 && index == posts.size - 10) {
                    LaunchedEffect(key1 = index) {
                        onLoadMore()
                    }
                }
            }
        }
    }
}
