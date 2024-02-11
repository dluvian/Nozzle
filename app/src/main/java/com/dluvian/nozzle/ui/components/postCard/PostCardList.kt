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
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.pullRefresh.PullRefreshBox

@Composable
fun PostCardList(
    posts: List<PostWithMeta>,
    showProfilePicture: Boolean,
    isRefreshing: Boolean,
    postCardLambdas: PostCardLambdas,
    onRefresh: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLoadMore: () -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            itemsIndexed(items = posts, key = { _, item -> item.entity.id }) { index, post ->
                PostCard(
                    post = post,
                    showProfilePicture = showProfilePicture,
                    postCardLambdas = postCardLambdas,
                    onPrepareReply = onPrepareReply,
                )
                if (index == posts.size - 3 && posts.size >= DB_BATCH_SIZE / 2) onLoadMore()
            }
        }
    }
}
