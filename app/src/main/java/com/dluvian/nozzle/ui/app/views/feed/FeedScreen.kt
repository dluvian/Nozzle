package com.dluvian.nozzle.ui.app.views.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.bars.FeedTopBar
import com.dluvian.nozzle.ui.components.buttons.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.drawer.FeedFilterDrawer
import com.dluvian.nozzle.ui.components.fabs.CreateNoteFab
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.postCard.PostCardList
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    uiState: FeedViewModelState,
    filterLambdas: FeedFilterLambdas,
    pubkey: String,
    picture: String?,
    showProfilePicture: Boolean,
    feed: List<PostWithMeta>,
    numOfNewPosts: Int,
    postCardLambdas: PostCardLambdas,
    onRefresh: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLoadMore: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToPost: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            FeedTopBar(
                pubkey = pubkey,
                picture = picture,
                showProfilePicture = showProfilePicture,
                onToggleFilterDrawer = {
                    scope.launch { drawerState.apply { if (isOpen) close() else open() } }
                },
                onPictureClick = onOpenDrawer,
                onScrollToTop = { scope.launch { lazyListState.animateScrollToItem(0) } }
            )
        },
        floatingActionButton = { CreateNoteFab(onCreateNote = onNavigateToPost) },
    ) {
        FeedFilterDrawer(
            modifier = Modifier.padding(it),
            drawerState = drawerState,
            uiState = uiState,
            filterLambdas = filterLambdas,
            onApplyAndClose = {
                onRefresh()
                scope.launch { drawerState.close() }
            }
        ) {
            FeedContent(
                feed = feed,
                uiState = uiState,
                showProfilePicture = showProfilePicture,
                numOfNewPosts = numOfNewPosts,
                paddingValues = it,
                lazyListState = lazyListState,
                postCardLambdas = postCardLambdas,
                onRefresh = onRefresh,
                onPrepareReply = onPrepareReply,
                onLoadMore = onLoadMore
            )
        }
    }
}

@Composable
private fun FeedContent(
    feed: List<PostWithMeta>,
    showProfilePicture: Boolean,
    uiState: FeedViewModelState,
    numOfNewPosts: Int,
    paddingValues: PaddingValues,
    lazyListState: LazyListState,
    postCardLambdas: PostCardLambdas,
    onRefresh: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onLoadMore: () -> Unit,
) {
    ShowNewPostsButton(
        numOfNewPosts = numOfNewPosts,
        isRefreshing = uiState.isRefreshing,
        feedSize = feed.size,
        lazyListState = lazyListState,
        onRefresh = onRefresh
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        PostCardList(
            posts = feed,
            showProfilePicture = showProfilePicture,
            isRefreshing = uiState.isRefreshing,
            postCardLambdas = postCardLambdas,
            onRefresh = onRefresh,
            onPrepareReply = onPrepareReply,
            onLoadMore = onLoadMore,
            lazyListState = lazyListState,
        )
    }
    NoPostsHint(feed = feed, isRefreshing = uiState.isRefreshing)
}
