package com.dluvian.nozzle.ui.app.views.inbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RelaySelection
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.buttons.ShowNewPostsButton
import com.dluvian.nozzle.ui.components.dropdown.RelayDropdown
import com.dluvian.nozzle.ui.components.fabs.CreateNoteFab
import com.dluvian.nozzle.ui.components.hint.NoPostsHint
import com.dluvian.nozzle.ui.components.iconButtons.RelayIconButton
import com.dluvian.nozzle.ui.components.postCard.PostCardList
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold

@Composable
fun InboxScreen(
    uiState: InboxViewModelState,
    feed: List<PostWithMeta>,
    numOfNewPosts: Int,
    postCardLambdas: PostCardLambdas,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val showRelayMenu = remember { mutableStateOf(false) }
    ReturnableScaffold(
        topBarText = stringResource(id = R.string.inbox),
        onGoBack = onGoBack,
        fab = { CreateNoteFab(onCreateNote = postCardLambdas.navLambdas.onNavigateToPost) },
        actions = {
            RelayIconButton(
                onClick = { showRelayMenu.value = true },
                description = stringResource(id = R.string.show_relays)
            )
            // TODO: FilterDrawer like in FeedScreen
        }
    ) {
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            RelayDropdown(
                showMenu = showRelayMenu.value,
                relays = remember(uiState.relays) {
                    uiState.relays.map { relay ->
                        RelaySelection(
                            relay = relay,
                            isActive = true
                        )
                    }
                },
                isEnabled = false,
                onDismiss = { showRelayMenu.value = false },
                onToggleIndex = {}
            )
        }
        val lazyListState = rememberLazyListState()
        ShowNewPostsButton(
            numOfNewPosts = numOfNewPosts,
            isRefreshing = uiState.isRefreshing,
            feedSize = feed.size,
            lazyListState = lazyListState,
            onRefresh = onRefresh
        )
        Column(modifier = Modifier.fillMaxSize()) {
            PostCardList(
                posts = feed,
                isRefreshing = uiState.isRefreshing,
                postCardLambdas = postCardLambdas,
                onRefresh = onRefresh,
                onPrepareReply = onPrepareReply,
                onLoadMore = onLoadMore,
                lazyListState = lazyListState
            )
        }
        NoPostsHint(feed = feed, isRefreshing = uiState.isRefreshing)
    }
}
