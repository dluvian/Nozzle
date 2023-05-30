package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PullRefreshBox(isRefreshing: Boolean, onRefresh: () -> Unit, content: @Composable () -> Unit) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing, onRefresh = onRefresh
    )
    Box(Modifier.pullRefresh(state = pullRefreshState)) {
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f),
        )
        content()
    }
}