package com.dluvian.nozzle.ui.app.views.feed

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.model.RelayActive

@Immutable
data class FeedViewModelState(
    val isRefreshing: Boolean = false,
    val relayStatuses: List<RelayActive> = emptyList(),
    val isPosts: Boolean = true,
    val isReplies: Boolean = true,
    val isFriends: Boolean = true,
    val isFriendCircle: Boolean = false,
    val isGlobal: Boolean = false,
    val isAutopilot: Boolean = true,
    val isReadRelays: Boolean = false,

    )
