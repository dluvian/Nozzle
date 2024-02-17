package com.dluvian.nozzle.ui.app.views.relayProfile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.ItemWithOnlineStatus
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.relay.RelayProfile
import com.dluvian.nozzle.ui.components.pullRefresh.PullRefreshBox
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold

@Composable
fun RelayProfileScreen(
    relayProfile: ItemWithOnlineStatus<RelayProfile?>,
    relay: Relay,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onGoBack: () -> Unit
) {
    ReturnableScaffold(
        topBarText = stringResource(id = R.string.relay_profile),
        onGoBack = onGoBack,
    ) {
        PullRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyListState()
            ) {
                item { Text(text = relay) }
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
            }
        }
    }
}
