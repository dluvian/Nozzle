package com.dluvian.nozzle.ui.app.views.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun InboxRoute(
    inboxViewModel: InboxViewModel,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by inboxViewModel.uiState.collectAsState()
    val feedFlow by inboxViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    InboxScreen(
        uiState = uiState,
        feed = feed,
        postCardNavLambdas = postCardNavLambdas,
        onLike = { post ->
            inboxViewModel.postCardInteractor.like(
                scope = inboxViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onShowMedia = { mediaUrl ->
            inboxViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            inboxViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefresh = inboxViewModel.onRefresh,
        onLoadMore = inboxViewModel.onLoadMore,
        onPrepareReply = onPrepareReply,
        onGoBack = onGoBack
    )
}
