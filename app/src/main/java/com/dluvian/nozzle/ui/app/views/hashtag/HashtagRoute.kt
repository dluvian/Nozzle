package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas

@Composable
fun HashtagRoute(
    hashtagViewModel: HashtagViewModel,
    profileFollower: IProfileFollower,
    postCardLambdas: PostCardLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by hashtagViewModel.uiState.collectAsState()
    val feedFlow by hashtagViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()
    val forceFollowed by profileFollower.getForceFollowedState()
    val adjustedFeed = remember(forceFollowed, feed) {
        feed.map { it.copy(isFollowedByMe = forceFollowed[it.pubkey] ?: it.isFollowedByMe) }
    }

    HashtagScreen(
        uiState = uiState,
        feed = adjustedFeed,
        postCardLambdas = postCardLambdas,
        onRefresh = hashtagViewModel.onRefresh,
        onLoadMore = hashtagViewModel.onLoadMore,
        onPrepareReply = onPrepareReply,
        onGoBack = onGoBack
    )
}
