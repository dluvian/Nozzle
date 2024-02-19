package com.dluvian.nozzle.ui.components.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.Z_INDEX_UNDER_PULL_REFRESH
import com.dluvian.nozzle.ui.components.isScrollingUp
import kotlinx.coroutines.launch

@Composable
fun ShowNewPostsButton(
    numOfNewPosts: Int,
    isRefreshing: Boolean,
    feedSize: Int,
    lazyListState: LazyListState,
    onRefresh: () -> Unit
) {
    val isScrollingUp = lazyListState.isScrollingUp()
    val isVisible = remember(isRefreshing) {
        !isRefreshing && numOfNewPosts > 0 && (feedSize < DB_BATCH_SIZE || isScrollingUp)
    }
    Box(
        modifier = Modifier
            .zIndex(Z_INDEX_UNDER_PULL_REFRESH)
            .fillMaxWidth()
            .fillMaxHeight(0.15f), contentAlignment = Alignment.BottomCenter
    ) {
        val scope = rememberCoroutineScope()
        AnimatedVisibility(visible = isVisible) {
            BaseButton(
                text = "$numOfNewPosts ${stringResource(id = R.string.new_posts)}",
                onClick = {
                    onRefresh()
                    scope.launch { lazyListState.animateScrollToItem(0) }
                }
            )
        }
    }
}
