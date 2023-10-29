package com.dluvian.nozzle.ui.app.views.feed

import com.dluvian.nozzle.model.Contacts
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.UserSpecific

data class FeedViewModelState(
    val isRefreshing: Boolean = false,
    val feedSettings: FeedSettings = FeedSettings(
        isPosts = true,
        isReplies = true,
        hashtag = null,
        authorSelection = Contacts,
        relaySelection = UserSpecific(emptyMap())
    ),
    val relayStatuses: List<RelayActive> = emptyList(),
)
