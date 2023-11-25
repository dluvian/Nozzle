package com.dluvian.nozzle.ui.components.postCard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas
import com.dluvian.nozzle.ui.components.PullRefreshBox

@Composable
fun PostCardList(
    posts: List<PostWithMeta>,
    isRefreshing: Boolean,
    postCardNavLambdas: PostCardNavLambdas,
    onRefresh: () -> Unit,
    onLike: (PostWithMeta) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLoadMore: () -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), state = lazyListState
        ) {
            itemsIndexed(items = posts, key = { _, item -> item.entity.id }) { index, post ->
                PostCard(
                    post = post,
                    postCardNavLambdas = postCardNavLambdas,
                    onLike = { onLike(post) },
                    onPrepareReply = onPrepareReply,
                    onShowMedia = onShowMedia,
                    onShouldShowMedia = onShouldShowMedia
                )
                if (index == posts.size - 3 && posts.size >= DB_BATCH_SIZE) onLoadMore()
            }
        }
    }
}
