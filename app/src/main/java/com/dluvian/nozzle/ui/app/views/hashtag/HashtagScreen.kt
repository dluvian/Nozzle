package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.postCard.PostCardList


@Composable
fun HashtagScreen(
    uiState: HashtagViewModelState,
    feedState: List<PostWithMeta>,
    onLike: (PostWithMeta) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToThread: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReply: (String) -> Unit,
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
                posts = feedState,
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                onLike = onLike,
                onShowMedia = onShowMedia,
                onShouldShowMedia = onShouldShowMedia,
                onPrepareReply = { /*TODO: Delete dis*/ },
                onLoadMore = onLoadMore,
                onNavigateToThread = onNavigateToThread,
                onNavigateToReply = { /*TODO: Adjust this*/ }, //onNavigateToReply,
                onNavigateToQuote = onNavigateToQuote,
                lazyListState = lazyListState,
                onNavigateToId = onNavigateToId,
                onOpenProfile = onNavigateToProfile,
            )
        }
    }
}
