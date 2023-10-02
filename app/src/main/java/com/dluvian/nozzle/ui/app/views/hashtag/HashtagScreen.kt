package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.postCard.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList


@Composable
fun HashtagScreen(
    uiState: HashtagViewModelState,
    feed: List<PostWithMeta>,
    onLike: (PostWithMeta) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onNavigateToThread: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReply: () -> Unit,
    onNavigateToQuote: (String) -> Unit,
    onNavigateToId: (String) -> Unit,
    onGoBack: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Column {
        val title = remember(uiState.feedSettings.hashtag) {
            "#${uiState.feedSettings.hashtag.orEmpty()}"
        }
        ReturnableTopBar(text = title, onGoBack = onGoBack)
        Column(modifier = Modifier.fillMaxSize()) {
            PostCardList(
                posts = feed,
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                onLike = onLike,
                onShowMedia = onShowMedia,
                onShouldShowMedia = onShouldShowMedia,
                onPrepareReply = onPrepareReply, // TODO: Delete dis
                onLoadMore = onLoadMore,
                onNavigateToThread = onNavigateToThread,
                onNavigateToReply = onNavigateToReply, // TODO: Adjust this. 2 in 1 lambda
                onNavigateToQuote = onNavigateToQuote,
                lazyListState = lazyListState,
                onNavigateToId = onNavigateToId,
                onOpenProfile = onNavigateToProfile,
            )
        }
    }
    if (feed.isEmpty()) {
        NoPostsHint()
    }
}
