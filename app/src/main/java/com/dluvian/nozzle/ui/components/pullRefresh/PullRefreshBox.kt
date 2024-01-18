package com.dluvian.nozzle.ui.components.pullRefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRefreshBox(isRefreshing: Boolean, onRefresh: () -> Unit, content: @Composable () -> Unit) {
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            if (!isRefreshing) onRefresh()
        }
    }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) pullRefreshState.startRefresh()
        else pullRefreshState.endRefresh()
    }
    // Indicator rests under TopAppBar
    // but if no TopAppBar is present (ProfileScreen) you will still see it
    // TODO: Check if newer material3 versions fix this
    val forceTransparency = !isRefreshing &&
            remember { derivedStateOf { pullRefreshState.progress == 0f } }.value
    Box(Modifier.nestedScroll(pullRefreshState.nestedScrollConnection)) {
        content()
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullRefreshState,
            containerColor = if (forceTransparency) Color.Transparent else PullToRefreshDefaults.containerColor,
            contentColor = if (forceTransparency) Color.Transparent else PullToRefreshDefaults.contentColor
        )
    }
}
