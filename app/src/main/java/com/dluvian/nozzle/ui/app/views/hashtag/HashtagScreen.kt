package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.utils.isScrollingUp
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList


@Composable
fun HashtagScreen(
    uiState: HashtagViewModelState,
    feed: List<PostWithMeta>,
    numOfNewPosts: Int,
    postCardLambdas: PostCardLambdas,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    ShowNewPostsButton(
        isVisible = !uiState.isRefreshing && numOfNewPosts > 0
                && (feed.size < DB_BATCH_SIZE || lazyListState.isScrollingUp()),
        numOfNewPosts = numOfNewPosts,
        lazyListState = lazyListState,
        onRefresh = onRefresh
    )
    Column {
        val title = remember(uiState.feedSettings.hashtag) {
            "#${uiState.feedSettings.hashtag.orEmpty()}"
        }
        ReturnableTopBar(text = title, onGoBack = onGoBack)
        Column(modifier = Modifier.fillMaxSize()) {
            PostCardList(
                posts = feed,
                isRefreshing = uiState.isRefreshing,
                postCardLambdas = postCardLambdas,
                onRefresh = onRefresh, // TODO: Delete dis
                onPrepareReply = onPrepareReply,
                onLoadMore = onLoadMore,
                lazyListState = lazyListState
            )
        }
    }
    if (feed.isEmpty()) NoPostsHint()
}
