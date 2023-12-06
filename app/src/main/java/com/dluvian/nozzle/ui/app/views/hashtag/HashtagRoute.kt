package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas

@Composable
fun HashtagRoute(
    hashtagViewModel: HashtagViewModel,
    profileFollower: IProfileFollower,
    postCardNavLambdas: PostCardNavLambdas,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val uiState by hashtagViewModel.uiState.collectAsState()
    val feedFlow by hashtagViewModel.feed.collectAsState()
    val feed by feedFlow.collectAsState()

    HashtagScreen(
        uiState = uiState,
        feed = feed,
        postCardNavLambdas = postCardNavLambdas,
        onLike = { post ->
            hashtagViewModel.postCardInteractor.like(
                scope = hashtagViewModel.viewModelScope,
                postId = post.entity.id,
                postPubkey = post.pubkey
            )
        },
        onShowMedia = { mediaUrl ->
            hashtagViewModel.clickedMediaUrlCache.insert(mediaUrl)
        },
        onShouldShowMedia = { mediaUrl ->
            hashtagViewModel.clickedMediaUrlCache.contains(mediaUrl)
        },
        onRefresh = hashtagViewModel.onRefresh,
        onLoadMore = hashtagViewModel.onLoadMore,
        onPrepareReply = onPrepareReply,
        onFollow = { pubkeyToFollow: Pubkey ->
            profileFollower.follow(
                scope = hashtagViewModel.viewModelScope,
                pubkeyToFollow = pubkeyToFollow
            )
        },
        onUnfollow = { pubkeyToUnfollow: Pubkey ->
            profileFollower.unfollow(
                scope = hashtagViewModel.viewModelScope,
                pubkeyToUnfollow = pubkeyToUnfollow
            )
        },
        onGoBack = onGoBack
    )
}
