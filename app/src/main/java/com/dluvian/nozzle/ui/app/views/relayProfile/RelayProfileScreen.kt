package com.dluvian.nozzle.ui.app.views.relayProfile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.ItemWithOnlineStatus
import com.dluvian.nozzle.model.relay.RelayProfile
import com.dluvian.nozzle.ui.components.indicators.TopBarCircleProgressIndicator
import com.dluvian.nozzle.ui.components.pullRefresh.PullRefreshBox
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun RelayProfileScreen(
    relayProfile: ItemWithOnlineStatus<RelayProfile?>,
    uiState: RelayProfileViewModelState,
    onRefresh: () -> Unit,
    onAddToNip65: () -> Unit,
    onGoBack: () -> Unit
) {
    ReturnableScaffold(
        topBarText = stringResource(id = R.string.relay_profile),
        onGoBack = onGoBack,
        actions = {
            if (uiState.isUpdatingNip65) TopBarCircleProgressIndicator()
        }
    ) {
        PullRefreshBox(isRefreshing = uiState.isRefreshing, onRefresh = onRefresh) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.screenEdge),
                state = rememberLazyListState()
            ) {
                item { Text(text = uiState.relay) }
                item { Text(text = relayProfile.onlineStatus.toString()) }
                item { Text(text = relayProfile.item?.name.orEmpty()) }
                item { Text(text = relayProfile.item?.description.orEmpty()) }
                item { Text(text = relayProfile.item?.pubkey.orEmpty()) }
                item { Text(text = relayProfile.item?.limitation?.authRequired.toString()) }
                item { Text(text = relayProfile.item?.limitation?.restrictedWrites.toString()) }
                item { Text(text = relayProfile.item?.paymentRequired.toString()) }
                item { Text(text = relayProfile.item?.paymentsUrl.orEmpty()) }
                item { Text(text = relayProfile.item?.software.orEmpty()) }
                item { Text(text = relayProfile.item?.version.orEmpty()) }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onAddToNip65,
                            enabled = uiState.isAddableToNip65
                        ) {
                            Text(text = stringResource(id = R.string.add_to_my_relays))
                        }
                    }
                }

            }
        }
    }
}
