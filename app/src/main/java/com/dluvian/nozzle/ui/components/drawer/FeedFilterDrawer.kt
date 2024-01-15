package com.dluvian.nozzle.ui.components.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.drawerFilter.CheckBoxFilterValue
import com.dluvian.nozzle.model.drawerFilter.FilterCategory
import com.dluvian.nozzle.model.drawerFilter.SwitchFilterValue
import com.dluvian.nozzle.ui.app.views.feed.FeedFilterLambdas
import com.dluvian.nozzle.ui.app.views.feed.FeedViewModelState

@Composable
fun FeedFilterDrawer(
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