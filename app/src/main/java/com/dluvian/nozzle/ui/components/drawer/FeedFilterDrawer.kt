package com.dluvian.nozzle.ui.components.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.drawerFilter.CheckBoxFilterValue
import com.dluvian.nozzle.model.drawerFilter.FilterCategory
import com.dluvian.nozzle.model.drawerFilter.RadioFilterValue
import com.dluvian.nozzle.ui.app.views.feed.FeedFilterLambdas
import com.dluvian.nozzle.ui.app.views.feed.FeedViewModelState

@Composable
fun FeedFilterDrawer(
    drawerState: DrawerState,
    uiState: FeedViewModelState,
    filterLambdas: FeedFilterLambdas,
    onApplyAndClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    FilterDrawer(
        modifier = modifier,
        drawerState = drawerState,
        filterCategories = listOf(
            FilterCategory(
                name = stringResource(id = R.string.type),
                filters = listOf(
                    CheckBoxFilterValue(
                        name = stringResource(id = R.string.posts),
                        isSelected = uiState.isPosts,
                        onClick = filterLambdas.onTogglePosts
                    ),
                    CheckBoxFilterValue(
                        name = stringResource(id = R.string.replies),
                        isSelected = uiState.isReplies,
                        onClick = filterLambdas.onToggleReplies
                    ),
                )
            ),
            FilterCategory(
                name = stringResource(id = R.string.people),
                filters = listOf(
                    RadioFilterValue(
                        name = stringResource(id = R.string.friends),
                        isSelected = uiState.isFriends,
                        onClick = filterLambdas.onToggleFriends
                    ),
                    RadioFilterValue(
                        name = stringResource(id = R.string.friend_circle),
                        isSelected = uiState.isFriendCircle,
                        onClick = filterLambdas.onToggleFriendCircle
                    ),
                    RadioFilterValue(
                        name = stringResource(id = R.string.global),
                        isSelected = uiState.isGlobal,
                        onClick = filterLambdas.onToggleGlobal
                    ),
                )
            ),
            FilterCategory(
                name = stringResource(id = R.string.relays),
                filters = listOf(
                    RadioFilterValue(
                        name = stringResource(id = R.string.autopilot),
                        isSelected = uiState.isAutopilot,
                        onClick = filterLambdas.onToggleAutopilot
                    ),
                    RadioFilterValue(
                        name = stringResource(id = R.string.my_read_relays),
                        isSelected = uiState.isReadRelays,
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