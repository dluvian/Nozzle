package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.buttons.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold


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
    ReturnableScaffold(
        topBarText = remember(uiState.hashtag) { "#${uiState.hashtag}" },
        onGoBack = onGoBack,
    ) {
        val lazyListState = rememberLazyListState()
        ShowNewPostsButton(
            numOfNewPosts = numOfNewPosts,
            isRefreshing = uiState.isRefreshing,
            feedSize = feed.size,
            lazyListState = lazyListState,
            onRefresh = onRefresh
        )
        PostCardList(
            posts = feed,
            isRefreshing = uiState.isRefreshing,
            postCardLambdas = postCardLambdas,
            onRefresh = onRefresh,
            onPrepareReply = onPrepareReply,
            onLoadMore = onLoadMore,
            lazyListState = lazyListState
        )
        NoPostsHint(feed = feed, isRefreshing = uiState.isRefreshing)
    }
}
