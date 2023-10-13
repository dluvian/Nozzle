package com.dluvian.nozzle.ui.app.views.hashtag

import com.dluvian.nozzle.model.Everyone
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.MultipleRelays

data class HashtagViewModelState(
    val isRefreshing: Boolean = false,
    val feedSettings: FeedSettings = FeedSettings(
        isPosts = true,
        isReplies = true,
        hashtag = null,
        authorSelection = Everyone,
        relaySelection = MultipleRelays(emptyList())
    )
)
