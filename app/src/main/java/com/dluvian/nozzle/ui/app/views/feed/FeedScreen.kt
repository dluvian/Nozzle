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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.utils.isScrollingUp
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.AddIcon
import com.dluvian.nozzle.ui.components.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.drawer.CheckBoxFilterValue
import com.dluvian.nozzle.ui.components.drawer.FilterCategory
import com.dluvian.nozzle.ui.components.drawer.FilterDrawer
import com.dluvian.nozzle.ui.components.drawer.SwitchFilterValue
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.components.postCard.PostCardList
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    uiState: FeedViewModelState,
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
        FilterDrawer(
            drawerState = drawerState,
            filterCategories = listOf(
                FilterCategory(
                    name = "Type",
                    filters = listOf(
                        CheckBoxFilterValue(name = "Posts", isChecked = false, onClick = {}),
                        CheckBoxFilterValue(name = "Replies", isChecked = true, onClick = {}),
                    )
                ),
                FilterCategory(
                    name = "People",
                    filters = listOf(
                        SwitchFilterValue(name = "Global", isChecked = false, onClick = {}),
                        SwitchFilterValue(name = "Friends", isChecked = true, onClick = {}),
                        SwitchFilterValue(name = "Friend circle", isChecked = false, onClick = {}),
                    )
                ),
                FilterCategory(
                    name = "Relays",
                    filters = listOf(
                        SwitchFilterValue(name = "Autopilot", isChecked = false, onClick = {}),
                        SwitchFilterValue(name = "My read relays", isChecked = true, onClick = {}),
                        SwitchFilterValue(
                            name = "Custom relay set",
                            isChecked = false,
                            onClick = {}),
                    ),
                    onAdd = {}
                ),
            ),
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

@Composable
private fun FeedTopBar(
    pubkey: String,
    onFilterDrawer: () -> Unit,
    onPictureClick: () -> Unit,
    onScrollToTop: () -> Unit
) {
    TopAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePicture(
                modifier = Modifier
                    .weight(weight = 0.15f, fill = false)
                    .padding(start = spacing.large)
                    .size(sizing.smallProfilePicture)
                    .clip(CircleShape)
                    .clickable(onClick = onPictureClick),
                pubkey = pubkey,
                trustType = Oneself
            )
            Headline(
                modifier = Modifier.weight(0.7f),
                headline = stringResource(id = R.string.Feed),
                onScrollToTop = onScrollToTop,
            )
            Icon(
                modifier = Modifier
                    .weight(weight = 0.15f, fill = false)
                    .padding(end = spacing.large)
                    .clip(CircleShape)
                    .clickable(onClick = onFilterDrawer),
                imageVector = Icons.Default.SettingsSuggest,
                contentDescription = stringResource(id = R.string.feed_settings),
            )
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
