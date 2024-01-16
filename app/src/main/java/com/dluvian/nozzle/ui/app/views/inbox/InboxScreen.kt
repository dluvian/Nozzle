package com.dluvian.nozzle.ui.app.views.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.utils.isScrollingUp
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.bars.ReturnableTopBar
import com.dluvian.nozzle.ui.components.buttons.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.iconButtons.RelayIconButton
import com.dluvian.nozzle.ui.components.postCard.PostCardList

@Composable
fun InboxScreen(
    uiState: InboxViewModelState,
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
    val showRelayMenu = remember { mutableStateOf(false) }
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.inbox),
            onGoBack = onGoBack,
            actions = {
                RelayIconButton(
                    onClick = { showRelayMenu.value = true },
                    description = stringResource(id = R.string.show_relays)
                )
            })
        Column(modifier = Modifier.fillMaxSize()) {
            PostCardList(
                posts = feed,
                isRefreshing = uiState.isRefreshing,
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
