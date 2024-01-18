package com.dluvian.nozzle.ui.app.views.likes

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.buttons.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold

@Composable
fun LikesScreen(
    feed: List<PostWithMeta>,
    numOfNewPosts: Int,
    likeCount: Int,
    isRefreshing: Boolean,
    postCardLambdas: PostCardLambdas,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val baseTitle = stringResource(id = R.string.likes)
    val fullTitle = remember(likeCount) {
        val countStr = if (likeCount > 0) " ($likeCount)" else ""
        "$baseTitle$countStr"
    }
    ReturnableScaffold(
        topBarText = fullTitle,
        onGoBack = onGoBack,
    ) {
        val lazyListState = rememberLazyListState()
        ShowNewPostsButton(
            numOfNewPosts = numOfNewPosts,
            isRefreshing = isRefreshing,
            feedSize = feed.size,
            lazyListState = lazyListState,
            onRefresh = onRefresh
        )
        PostCardList(
            posts = feed,
            isRefreshing = isRefreshing,
            postCardLambdas = postCardLambdas,
            onRefresh = onRefresh,
            onPrepareReply = onPrepareReply,
            onLoadMore = onLoadMore,
            lazyListState = lazyListState
        )
        NoPostsHint(feed = feed, isRefreshing = isRefreshing)
    }
}
