package com.dluvian.nozzle.ui.app.views.likes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun LikesRoute(
    likesViewModel: LikesViewModel,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by likesViewModel.uiState.collectAsState()
    val likedPosts by likesViewModel.likedPosts.collectAsState()

    LikesScreen(
        uiState = uiState,
        likedPosts = likedPosts,
        postCardNavLambdas = postCardNavLambdas,
        onShowMedia = { mediaUrl ->
            likesViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            likesViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefresh = likesViewModel.onRefresh,
        onLoadMore = likesViewModel.onLoadMore,
        onPrepareReply = onPrepareReply,
        onGoBack = onGoBack
    )
}
