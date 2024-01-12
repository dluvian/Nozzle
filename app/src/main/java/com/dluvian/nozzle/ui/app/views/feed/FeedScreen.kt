package com.dluvian.nozzle.ui.app.views.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.utils.isScrollingUp
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.drawerFilter.CheckBoxFilterValue
import com.dluvian.nozzle.model.drawerFilter.FilterCategory
import com.dluvian.nozzle.model.drawerFilter.SwitchFilterValue
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.buttons.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.drawer.FilterDrawer
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.icons.AddIcon
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.components.postCard.PostCardList
import com.dluvian.nozzle.ui.theme.sizing
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    uiState: FeedViewModelState,
    filterLambdas: FeedFilterLambdas,
    pubkey: String,
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
                onFilterDrawer = {
                    scope.launch { drawerState.apply { if (isOpen) close() else open() } }
                },
                onPictureClick = onOpenDrawer,
                onScrollToTop = { scope.launch { lazyListState.animateScrollToItem(0) } }
            )
        },
        floatingActionButton = { FeedFab(onNavigateToPost = onNavigateToPost) },
    ) {
        FeedFilterDrawer(
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
private fun FeedFilterDrawer(
    drawerState: DrawerState,
    uiState: FeedViewModelState,
    filterLambdas: FeedFilterLambdas,
    onApplyAndClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    FilterDrawer(
        drawerState = drawerState,
        filterCategories = listOf(
            FilterCategory(
                name = stringResource(id = R.string.type),
                filters = listOf(
                    CheckBoxFilterValue(
                        name = stringResource(id = R.string.posts),
                        isChecked = uiState.isPosts,
                        onClick = filterLambdas.onTogglePosts
                    ),
                    CheckBoxFilterValue(
                        name = stringResource(id = R.string.replies),
                        isChecked = uiState.isReplies,
                        onClick = filterLambdas.onToggleReplies
                    ),
                )
            ),
            FilterCategory(
                name = stringResource(id = R.string.people),
                filters = listOf(
                    SwitchFilterValue(
                        name = stringResource(id = R.string.friends),
                        isChecked = uiState.isFriends,
                        isEnabled = !uiState.isFriends,
                        onClick = filterLambdas.onToggleFriends
                    ),
                    SwitchFilterValue(
                        name = stringResource(id = R.string.friend_circle),
                        isChecked = uiState.isFriendCircle,
                        isEnabled = !uiState.isFriendCircle,
                        onClick = filterLambdas.onToggleFriendCircle
                    ),
                    SwitchFilterValue(
                        name = stringResource(id = R.string.global),
                        isChecked = uiState.isGlobal,
                        isEnabled = !uiState.isGlobal,
                        onClick = filterLambdas.onToggleGlobal
                    ),
                )
            ),
            FilterCategory(
                name = stringResource(id = R.string.relays),
                filters = listOf(
                    SwitchFilterValue(
                        name = stringResource(id = R.string.autopilot),
                        isChecked = uiState.isAutopilot,
                        onClick = filterLambdas.onToggleAutopilot
                    ),
                    SwitchFilterValue(
                        name = stringResource(id = R.string.my_read_relays),
                        isChecked = uiState.isReadRelays,
                        onClick = filterLambdas.onToggleReadRelays
                    ),
                ),
            ),
        ),
        onClose = onApplyAndClose
    ) {
        content()
    }
}

@Composable
private fun FeedContent(
    feed: List<PostWithMeta>,
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
        isVisible = !uiState.isRefreshing && numOfNewPosts > 0
                && (feed.size < DB_BATCH_SIZE || lazyListState.isScrollingUp()),
        numOfNewPosts = numOfNewPosts,
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
            isRefreshing = uiState.isRefreshing,
            postCardLambdas = postCardLambdas,
            onRefresh = onRefresh,
            onPrepareReply = onPrepareReply,
            onLoadMore = onLoadMore,
            lazyListState = lazyListState,
        )
    }
    if (feed.isEmpty()) NoPostsHint()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTopBar(
    pubkey: String,
    onFilterDrawer: () -> Unit,
    onPictureClick: () -> Unit,
    onScrollToTop: () -> Unit
) {
    TopAppBar(title = {
        Text(
            modifier = Modifier.clickable(onClick = onScrollToTop),
            text = stringResource(id = R.string.Feed)
        )
    }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.weight(weight = 0.15f, fill = false),
                onClick = onPictureClick
            ) {
                ProfilePicture(
                    modifier = Modifier.size(sizing.smallProfilePicture),
                    pubkey = pubkey,
                    trustType = Oneself
                )
            }
            Headline(
                modifier = Modifier.weight(0.7f),
                headline = stringResource(id = R.string.Feed),
                onScrollToTop = onScrollToTop,
            )
            IconButton(
                modifier = Modifier.weight(weight = 0.15f, fill = false),
                onClick = onFilterDrawer
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.feed_settings),
                )
            }
        }
    }
}

@Composable
private fun Headline(
    headline: String,
    onScrollToTop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Text(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onScrollToTop() },
            text = headline.removePrefix("wss://"),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = typography.h6,
            color = Color.White,
        )
    }
}

@Composable
private fun FeedFab(onNavigateToPost: () -> Unit) {
    FloatingActionButton(onClick = onNavigateToPost, contentColor = Color.White) {
        AddIcon()
    }
}
