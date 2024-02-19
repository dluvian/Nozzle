package com.dluvian.nozzle.ui.app.views.relayProfile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.url),
                        value = uiState.relay
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.status),
                        value = relayProfile.onlineStatus.toString()
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.name),
                        value = relayProfile.item?.name
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.description),
                        value = relayProfile.item?.description
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.admin),
                        value = relayProfile.item?.pubkey
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.auth_required),
                        value = booleanString(value = relayProfile.item?.limitation?.authRequired)
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.restricted_writes),
                        value = booleanString(relayProfile.item?.limitation?.restrictedWrites)
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.payment_required),
                        value = booleanString(relayProfile.item?.limitation?.paymentRequired)
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.payment_url),
                        value = relayProfile.item?.paymentsUrl
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.software),
                        value = relayProfile.item?.software
                    )
                }
                item {
                    InfoRow(
                        infoType = stringResource(id = R.string.version),
                        value = relayProfile.item?.version
                    )
                }
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


@Composable
private fun InfoRow(infoType: String, value: String?) {
    Row(modifier = Modifier.padding(vertical = spacing.medium)) {
        Column(modifier = Modifier.weight(weight = 0.3f, fill = true)) {
            Text(text = infoType, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(spacing.medium))
        Column(modifier = Modifier.weight(weight = 0.7f, fill = true)) {
            if (value != null) Text(text = value)
            else Text(text = stringResource(id = R.string.unknown))
        }
    }
}

@Composable
private fun booleanString(value: Boolean?): String? {
    return when (value) {
        null -> null
        true -> stringResource(id = R.string.yes)
        false -> stringResource(id = R.string.no)
    }
}
