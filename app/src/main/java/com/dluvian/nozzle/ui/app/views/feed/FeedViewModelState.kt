package com.dluvian.nozzle.ui.app.views.feed

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.model.feedFilter.Autopilot
import com.dluvian.nozzle.model.feedFilter.FeedFilter
import com.dluvian.nozzle.model.feedFilter.FriendCircle
import com.dluvian.nozzle.model.feedFilter.Friends
import com.dluvian.nozzle.model.feedFilter.Global
import com.dluvian.nozzle.model.feedFilter.ReadRelays

@Immutable
data class FeedViewModelState(
    val isRefreshing: Boolean = false,
    val isPosts: Boolean = true,
    val isReplies: Boolean = true,
    val isFriends: Boolean = true,
    val isFriendCircle: Boolean = false,
    val isGlobal: Boolean = false,
    val isAutopilot: Boolean = true,
    val isReadRelays: Boolean = false,
) {
    companion object {
        fun from(feedFilter: FeedFilter): FeedViewModelState {
            return FeedViewModelState(
                isRefreshing = false,
                isPosts = feedFilter.isPosts,
                isReplies = feedFilter.isReplies,
                isFriends = feedFilter.authorFilter is Friends,
                isFriendCircle = feedFilter.authorFilter is FriendCircle,
                isGlobal = feedFilter.authorFilter is Global,
                isAutopilot = feedFilter.relayFilter is Autopilot,
                isReadRelays = feedFilter.relayFilter is ReadRelays,
            )
        }
    }
}
