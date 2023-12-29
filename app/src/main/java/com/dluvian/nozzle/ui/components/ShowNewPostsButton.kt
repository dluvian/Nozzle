package com.dluvian.nozzle.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.Z_INDEX_UNDER_PULL_REFRESH
import kotlinx.coroutines.launch

@Composable
fun ShowNewPostsButton(
    isVisible: Boolean,
    numOfNewPosts: Int,
    lazyListState: LazyListState,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .zIndex(Z_INDEX_UNDER_PULL_REFRESH)
            .fillMaxWidth()
            .fillMaxHeight(0.15f), contentAlignment = Alignment.BottomCenter
    ) {
        val scope = rememberCoroutineScope()
        AnimatedVisibility(visible = isVisible) {
            Button(onClick = {
                onRefresh()
                scope.launch { lazyListState.animateScrollToItem(0) }
            }) {
                Text("$numOfNewPosts ${stringResource(id = R.string.new_posts)}")
            }
        }
    }
}
