package com.dluvian.nozzle.ui.app.views.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.ShowRelaysButton
import com.dluvian.nozzle.ui.components.postCard.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList

@Composable
fun InboxScreen(
    uiState: InboxViewModelState,
    feed: List<PostWithMeta>,
    postCardNavLambdas: PostCardNavLambdas,
    onLike: (PostWithMeta) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.inbox),
            onGoBack = onGoBack,
            trailingIcon = {
                // Wrapped in Row or else dialog will be aligned to the left
                // TODO: Check if Row-wrap is still necessary
                Row {
                    ShowRelaysButton(relays = uiState.relays)
                }
            })
        Column(modifier = Modifier.fillMaxSize()) {
            PostCardList(
                posts = feed,
                isRefreshing = uiState.isRefreshing,
                postCardNavLambdas = postCardNavLambdas,
                onRefresh = onRefresh,
                onLike = onLike,
                onShowMedia = onShowMedia,
                onShouldShowMedia = onShouldShowMedia, // TODO: Delete dis
                onPrepareReply = onPrepareReply,
                onLoadMore = onLoadMore,
            )
        }
    }
    if (feed.isEmpty()) NoPostsHint()
}
