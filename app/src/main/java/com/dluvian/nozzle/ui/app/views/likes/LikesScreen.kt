package com.dluvian.nozzle.ui.app.views.likes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList

@Composable
fun LikesScreen(
    feed: List<PostWithMeta>,
    isRefreshing: Boolean,
    postCardNavLambdas: PostCardNavLambdas,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.likes),
            onGoBack = onGoBack
        )
        Column(modifier = Modifier.fillMaxSize()) {
            PostCardList(
                posts = feed,
                isRefreshing = isRefreshing,
                postCardNavLambdas = postCardNavLambdas,
                onRefresh = onRefresh,
                onLike = { /** Everything is already liked and likes are irreversable*/ },
                onShowMedia = onShowMedia,
                onShouldShowMedia = onShouldShowMedia,
                onPrepareReply = onPrepareReply,
                onLoadMore = onLoadMore,
            )
        }
    }
    if (feed.isEmpty()) NoPostsHint()
}
