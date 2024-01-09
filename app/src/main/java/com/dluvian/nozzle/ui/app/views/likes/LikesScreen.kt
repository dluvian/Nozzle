package com.dluvian.nozzle.ui.app.views.likes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.exoplayer.ExoPlayer
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.utils.isScrollingUp
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList

@Composable
fun LikesScreen(
    feed: List<PostWithMeta>,
    numOfNewPosts: Int,
    likeCount: Int,
    isRefreshing: Boolean,
    videoPlayer: ExoPlayer,
    postCardLambdas: PostCardLambdas,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    ShowNewPostsButton(
        isVisible = !isRefreshing && numOfNewPosts > 0
                && (feed.size < DB_BATCH_SIZE || lazyListState.isScrollingUp()),
        numOfNewPosts = numOfNewPosts,
        lazyListState = lazyListState,
        onRefresh = onRefresh
    )
    Column {
        val baseTitle = stringResource(id = R.string.likes)
        val fullTitle = remember(likeCount) {
            val countStr = if (likeCount > 0) " ($likeCount)" else ""
            "$baseTitle$countStr"
        }
        ReturnableTopBar(
            text = fullTitle,
            onGoBack = onGoBack
        )
        Column(modifier = Modifier.fillMaxSize()) {
            PostCardList(
                posts = feed,
                videoPlayer = videoPlayer,
                isRefreshing = isRefreshing,
                postCardLambdas = postCardLambdas,
                onRefresh = onRefresh,
                onPrepareReply = onPrepareReply,
                onLoadMore = onLoadMore,
                lazyListState = lazyListState
            )
        }
    }
    if (feed.isEmpty()) NoPostsHint()
}
