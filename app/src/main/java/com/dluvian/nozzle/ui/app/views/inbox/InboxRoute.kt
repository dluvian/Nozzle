package com.dluvian.nozzle.ui.app.views.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

@Composable
fun InboxRoute(
    inboxViewModel: InboxViewModel,
    profileFollower: IProfileFollower,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by inboxViewModel.uiState.collectAsState()

    val feedFlow by inboxViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    val numOfNewPostsFlow by inboxViewModel.numOfNewPosts.collectAsState()
    val numOfNewPosts by numOfNewPostsFlow.collectAsState()

    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedFeed = remember(forceFollowed, feed) {
        feed.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    InboxScreen(
        uiState = uiState,
        feed = adjustedFeed,
        numOfNewPosts = numOfNewPosts,
        postCardLambdas = postCardLambdas,
        onRefresh = inboxViewModel.onRefresh,
        onLoadMore = inboxViewModel.onLoadMore,
        onPrepareReply = onPrepareReply,
        onGoBack = onGoBack
    )
}
